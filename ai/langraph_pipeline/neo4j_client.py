from neo4j import GraphDatabase
from .config import NEO4J_URI, NEO4J_USER, NEO4J_PASS, NEO4J_DATABASE


class Neo4jClient:
    def __init__(self, uri=None, user=None, password=None, database=None):
        self._uri = uri or NEO4J_URI
        self._user = user or NEO4J_USER
        self._password = password or NEO4J_PASS
        self._database = database or NEO4J_DATABASE
        self._driver = GraphDatabase.driver(self._uri, auth=(self._user, self._password))

    def close(self):
        self._driver.close()

    def batch_query_by_surfaces(self, token_surfaces: list[str], detected_domains: list[str]) -> list[dict]:
        """
        Stateless batch query: lấy sense evidence cho list token surfaces.
        Không cần Sentence/Token node trong Neo4j.
        Query theo schema-neo4j.md Section 5.4.
        """
        q = """
        UNWIND $tokenSurfaces AS surface
        MATCH (lex:Lexeme {surface: surface})
          -[:HAS_SENSE]->(sense:Sense)
        OPTIONAL MATCH (sense)-[:BELONGS_TO]->(dom:Domain)
        OPTIONAL MATCH (sense)-[:HAS_REGISTER]->(reg:Register)
        OPTIONAL MATCH (sense)-[:SUPPORTED_BY]->(cue:Cue)
        OPTIONAL MATCH (sense)-[:HAS_EXAMPLE]->(ex:Example)
        OPTIONAL MATCH (sense)-[:REFERS_TO]->(ent:Entity)
        OPTIONAL MATCH (sense)-[:HAS_NOTE]->(note:CulturalNote)
        WITH surface, lex, sense, dom, reg, ent, note,
             collect(DISTINCT cue.surface) AS cues,
             collect(DISTINCT {
               ja: ex.ja,
               vi: ex.vi,
               quality: ex.qualityScore
             }) AS examples
        RETURN
          surface              AS token,
          lex.reading          AS reading,
          lex.jlpt             AS jlpt,
          lex.pos              AS pos,
          sense.senseId        AS senseId,
          sense.glossVi        AS glossVi,
          sense.glossEn        AS glossEn,
          sense.definition     AS definition,
          sense.usageNote      AS usageNote,
          sense.confidenceBase AS baseConfidence,
          dom.name             AS domain,
          reg.name             AS register,
          ent.name             AS entity,
          cues,
          examples,
          note.content         AS culturalNote
        ORDER BY token
        """
        with self._driver.session(database=self._database) as session:
            res = session.run(q, tokenSurfaces=token_surfaces)
            return [record.data() for record in res]

    def run_cypher(self, cypher, params=None):
        """Generic read-only query helper."""
        with self._driver.session(database=self._database) as session:
            res = session.run(cypher, params or {})
            return [r.data() for r in res]
