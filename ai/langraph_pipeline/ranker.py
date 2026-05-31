from collections import defaultdict

DEFAULT_WEIGHTS = {
    'domain':      0.30,
    'register':    0.15,
    'cue':         0.25,
    'collocation': 0.15,
    'example':     0.10,
    'base':        0.05,
}

# Domain → preferred register (dùng để boost register score)
DOMAIN_REGISTER_HINT = {
    'technology':    'technical',
    'semiconductor': 'technical',
    'medicine':      'technical',
    'culture':       'formal',
    'academic':      'formal',
}


class Ranker:
    def __init__(self, weights=None):
        self.w = weights or DEFAULT_WEIGHTS

    def score(self, sense_row: dict, detected_domains: list[str], neighbor_surfaces: set[str]) -> float:
        """
        Tính score cho một sense row từ Neo4j.
        Params:
            sense_row        : dict từ batch_query_by_surfaces
            detected_domains : list domain phát hiện in-memory
            neighbor_surfaces: set tất cả surface token trong đoạn (dùng cho cue matching)
        """
        score = 0.0
        detected_domains_set = set(detected_domains)

        # --- Domain match ---
        if sense_row.get('domain') in detected_domains_set:
            score += self.w['domain']

        # --- Register match ---
        reg = sense_row.get('register') or ''
        for dom in detected_domains:
            if DOMAIN_REGISTER_HINT.get(dom) == reg:
                score += self.w['register']
                break

        # --- Cue match: cue surface xuất hiện trong các token của đoạn ---
        cues = sense_row.get('cues') or []
        if cues:
            matched = sum(1 for c in cues if c in neighbor_surfaces)
            score += self.w['cue'] * (matched / len(cues))

        # --- Collocation: chưa implement, placeholder ---
        score += self.w['collocation'] * 0.0

        # --- Example quality ---
        examples = sense_row.get('examples') or []
        valid_examples = [e for e in examples if e.get('vi')]
        if valid_examples:
            avg_quality = sum(e.get('quality') or 0.8 for e in valid_examples) / len(valid_examples)
            score += self.w['example'] * avg_quality

        # --- Base confidence ---
        score += self.w['base'] * float(sense_row.get('baseConfidence') or 0.0)

        return round(score, 4)

    def rank(self, graph_evidence: list[dict], detected_domains: list[str]) -> tuple[list[dict], list[dict]]:
        """
        Rank tất cả sense từ graph evidence.
        Returns:
            ranked_senses  : [{surface, top_sense, score, alternatives}]
            key_vocabulary : [{surface, reading, jlpt, glossVi, domain, register}]
        """
        neighbor_surfaces = {row['token'] for row in graph_evidence}

        # Group theo token surface
        by_token: dict[str, list[dict]] = defaultdict(list)
        for row in graph_evidence:
            by_token[row['token']].append(row)

        ranked_senses = []
        key_vocabulary = []

        for surface, senses in by_token.items():
            # Score từng sense
            scored = sorted(
                [{'sense': s, 'score': self.score(s, detected_domains, neighbor_surfaces)} for s in senses],
                key=lambda x: x['score'],
                reverse=True,
            )
            top = scored[0]
            top_sense = top['sense']

            ranked_senses.append({
                'surface':      surface,
                'top_sense':    top_sense,
                'score':        top['score'],
                'alternatives': [s['sense'] for s in scored[1:3]],
            })

            # Xác định key vocabulary
            jlpt = top_sense.get('jlpt')
            is_polysemy = len(scored) >= 2 and scored[1]['score'] > 0
            is_domain_specific = top_sense.get('domain') in set(detected_domains)
            is_notable = (jlpt is not None and jlpt <= 3) or is_domain_specific or is_polysemy

            if is_notable:
                key_vocabulary.append({
                    'surface':  surface,
                    'reading':  top_sense.get('reading', ''),
                    'jlpt':     jlpt,
                    'glossVi':  top_sense.get('glossVi', ''),
                    'domain':   top_sense.get('domain'),
                    'register': top_sense.get('register'),
                })

        return ranked_senses, key_vocabulary
