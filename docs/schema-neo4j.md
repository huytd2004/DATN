# Schema Neo4j cho GraphRAG + LLM Translation

## Giới thiệu

Schema này được thiết kế cho hệ thống dịch machine-augmented chuyên ngành (kỹ thuật, văn hóa, y tế, etc.) sử dụng:
- **Neo4j** làm knowledge graph backend (chỉ lưu knowledge tĩnh)
- **GraphRAG** để retrieval context đa chiều theo token surface
- **LLM** để dịch với awareness ngữ cảnh

Mục tiêu: tránh dịch sai nghĩa (polysemy), bỏ lỡ collocation/idiom, và cung cấp evidence rõ ràng cho từng decision.

### Nguyên tắc thiết kế (Stateless Query)

> **Neo4j chỉ lưu knowledge graph tĩnh.** `Sentence` và `Token` từ user input KHÔNG được persist vào Neo4j.
> - Tokenization thực hiện in-memory (MeCab/sudachi).
> - Query Neo4j trực tiếp theo `surface` của từng token.
> - Lịch sử dịch (nếu cần) lưu ở **PostgreSQL**, không lưu Neo4j.

**Phân tách rõ ràng:**

| Loại dữ liệu | Lưu ở đâu |
|---|---|
| Knowledge graph (Lexeme, Sense, Domain...) | **Neo4j** (persist, bất biến) |
| Token, Sentence của user input | **In-memory** (runtime only) |
| Lịch sử bản dịch của user | **PostgreSQL** |

---

## 1. Node Types

### 1.1 Lexeme
**Định nghĩa:** Đơn vị từ bề mặt (surface form) của một từ Nhật Bản.

**Thuộc tính:**
```
{
  lexemeId: String,          # Unique ID, e.g. "lex_ハンダイ_1"
  surface: String,           # Surface form (e.g. "半導体")
  reading: String,           # Đọc furigana (e.g. "はんどうたい")
  pos: String,               # Part-of-speech (n, v, adj, adv, ...)
  jlpt: Integer,             # JLPT level (1-4 hoặc null)
  freq: Integer,             # Frequency rank từ corpus
  source: String,            # e.g. "jmdict", "crawled", "user_added"
  normalizedSurface: String  # Dùng cho deduplication
}
```

**Ví dụ:**
- `surface: "半導体"`, `reading: "はんどうたい"`, `pos: "n"` → semiconductor

---

### 1.2 Sense
**Định nghĩa:** Một nghĩa cụ thể của một Lexeme (xử lý polysemy).

**Thuộc tính:**
```
{
  senseId: String,           # Unique ID, e.g. "sense_semiconductor_1"
  glossVi: String,           # Gloss tiếng Việt (e.g. "bán dẫn")
  glossEn: String,           # Gloss tiếng Anh (e.g. "semiconductor")
  definition: String,        # Định nghĩa chi tiết
  domain: String,            # Domain chính (e.g. "technology", "medical")
  register: String,          # Register (e.g. "technical", "colloquial", "formal")
  confidenceBase: Float,     # Độ tin cậy nền tảng (0-1)
  usageNote: String          # Ghi chú sử dụng
}
```

**Ví dụ:**
- `glossVi: "bán dẫn"`, `domain: "technology"`, `register: "technical"`

---

### 1.3 Entity
**Định nghĩa:** Khái niệm hoặc thực thể tri thức trong lĩnh vực (e.g. abstract concept, physical thing).

**Thuộc tính:**
```
{
  entityId: String,          # Unique ID, e.g. "ent_semiconductor"
  name: String,              # Entity name
  type: String,              # e.g. "Concept", "Object", "Process", "Organization"
  description: String,       # Chi tiết về entity
  domain: String             # Domain liên quan
}
```

**Ví dụ:**
- `name: "Semiconductor"`, `type: "Concept"`, `domain: "technology"`

---

### 1.4 Domain
**Định nghĩa:** Miền ngữ cảnh (lĩnh vực chuyên ngành).

**Thuộc tính:**
```
{
  domainId: String,          # Unique ID, e.g. "domain_technology"
  name: String,              # e.g. "technology", "medicine", "culture"
  description: String,       # Mô tả lĩnh vực
  level: String              # e.g. "broad", "specific"
}
```

---

### 1.5 Register
**Định nghĩa:** Sắc thái sử dụng từ (formal/colloquial/technical/etc).

**Thuộc tính:**
```
{
  registerId: String,        # Unique ID, e.g. "reg_technical"
  name: String,              # e.g. "technical", "formal", "colloquial", "archaic"
  description: String
}
```

---

### 1.6 Cue
**Định nghĩa:** Tín hiệu ngữ cảnh giúp phân biệt nghĩa (collocation signals, topic markers).

**Thuộc tính:**
```
{
  cueId: String,             # Unique ID
  surface: String,           # e.g. "製造", "プロセス"
  category: String,          # e.g. "verb_companion", "noun_collocate", "topic_marker"
  weight: Float              # Mức độ quan trọng (0-1)
}
```

**Ví dụ:**
- `surface: "製造プロセス"` (manufacturing process) → signal cho "bán dẫn" trong domain kỹ thuật

---

### 1.7 Collocation
**Định nghĩa:** Cụm từ đi kèm thường gặp.

**Thuộc tính:**
```
{
  collocationId: String,     # Unique ID
  text: String,              # e.g. "半導体チップ", "性能を発揮する"
  glossVi: String,           # Dịch cụm (e.g. "chip bán dẫn", "phát huy hiệu năng")
  weight: Float              # Tần suất/độ tin cậy (0-1)
}
```

---

### 1.8 GrammarPattern
**Định nghĩa:** Mẫu ngữ pháp hoặc cấu trúc sử dụng.

**Thuộc tính:**
```
{
  patternId: String,         # Unique ID
  form: String,              # Mẫu (e.g. "N の N", "Vーdict と N")
  meaning: String,           # Nghĩa chung (e.g. "genitive relation", "quotative")
  level: String,             # JLPT level
  note: String               # Ghi chú
}
```

---

### 1.9 Example
**Định nghĩa:** Câu ví dụ đã có translation & tagging.

**Thuộc tính:**
```
{
  exampleId: String,         # Unique ID
  ja: String,                # Câu Nhật
  vi: String,                # Câu Việt
  en: String,                # Câu Anh (optional)
  source: String,            # e.g. "jmdict", "corpus", "user_added"
  qualityScore: Float,       # Chất lượng ví dụ (0-1)
  domain: String             # Domain áp dụng
}
```

---

### 1.10 CulturalNote
**Định nghĩa:** Ghi chú văn hóa, sắc thái sử dụng hay ngữ cảnh đặc biệt.

**Thuộc tính:**
```
{
  noteId: String,            # Unique ID
  title: String,             # Tiêu đề ghi chú
  content: String,           # Nội dung chi tiết
  domain: String,            # Domain liên quan
  level: String              # e.g. "beginner", "intermediate", "advanced"
}
```

---

### ~~1.11 Sentence~~ (Đã loại bỏ)
> Không persist vào Neo4j. Xử lý in-memory tại application layer.

### ~~1.12 Token~~ (Đã loại bỏ)
> Không persist vào Neo4j. Kết quả tokenization (MeCab/sudachi) được giữ in-memory và dùng trực tiếp để query Neo4j theo `surface`.

---

## 2. Relationship Types

### Lexeme & Sense
```
Lexeme -[:HAS_SENSE]-> Sense
  [role: "primary" | "secondary" | "archaic"]
```
Một từ có thể có nhiều nghĩa.

### Sense & Entity
```
Sense -[:REFERS_TO]-> Entity
```
Một sense chỉ đến một hoặc nhiều entity.

### Sense & Domain
```
Sense -[:BELONGS_TO]-> Domain
  [primary: Boolean]  # True nếu domain chính
```

### Sense & Register
```
Sense -[:HAS_REGISTER]-> Register
```

### Sense & Example
```
Sense -[:HAS_EXAMPLE]-> Example
  [illustrativeScore: Float]
```

### Sense & Cue
```
Sense -[:SUPPORTED_BY]-> Cue
  [strength: Float]  # 0-1
```
Cues hỗ trợ phân biệt sense này.

### Sense & Collocation
```
Sense -[:HAS_COLLOCATION]-> Collocation
  [frequency: Float]
```

### Sense & CulturalNote
```
Sense -[:HAS_NOTE]-> CulturalNote
```

### Example & Cue
```
Example -[:CONTAINS_CUE]-> Cue
```
Ví dụ này chứa các cue.

### Example & Domain
```
Example -[:IN_DOMAIN]-> Domain
```

### ~~Token & Lexeme~~ (Đã loại bỏ)
> Token không persist. Query trực tiếp `Lexeme {surface: $tokenSurface}`.

### ~~Sentence & Token~~ (Đã loại bỏ)
> Không persist Sentence/Token vào graph.

### Lexeme Relation (khác Lexeme)
```
Lexeme -[:RELATED_TO]-> Lexeme
  [type: "synonym" | "antonym" | "similar" | "derivation",
   weight: Float]
```

### Entity & CulturalNote
```
Entity -[:HAS_NOTE]-> CulturalNote
```

---

## 3. Thứ tự Import & Dependency

**Quan trọng:** Chỉ import knowledge graph tĩnh. `Sentence` và `Token` **không import**.

```
1. Domain, Register
   (No dependencies)

2. Entity
   (Depends on: Domain)

3. Lexeme
   (No dependencies)

4. Sense
   (Depends on: Lexeme, Domain, Register)

5. Cue, Collocation, GrammarPattern, CulturalNote
   (No dependencies hoặc minimal)

6. Example
   (Depends on: Domain)

7. Build relationships:
   - Sense -[:BELONGS_TO]-> Domain
   - Sense -[:REFERS_TO]-> Entity
   - Sense -[:HAS_REGISTER]-> Register
   - Sense -[:SUPPORTED_BY]-> Cue
   - Sense -[:HAS_EXAMPLE]-> Example
   - Sense -[:HAS_COLLOCATION]-> Collocation
   - Sense -[:HAS_NOTE]-> CulturalNote
   - Entity -[:HAS_NOTE]-> CulturalNote
   - Lexeme -[:HAS_SENSE]-> Sense
   - Lexeme -[:RELATED_TO]-> Lexeme
   - Example -[:IN_DOMAIN]-> Domain

[KHÔNG import: Sentence, Token — xử lý in-memory tại runtime]
```

---

## 4. Ví dụ Chi Tiết: Câu về Bán Dẫn

### Input
**Câu Nhật:**
```
半導体の製造プロセスでは、微細な欠陥が性能に大きく影響する。
```

**Dịch mong muốn:**
```
Trong quy trình sản xuất bán dẫn, các khuyết tật vi mô có ảnh hưởng lớn đến hiệu năng.
```

> **Runtime flow (stateless):**
> 1. MeCab tách ra: `["半導体", "の", "製造", "プロセス", "微細", "欠陥", "性能", "影響"]`
> 2. Detect domain: `"technology"`, `"semiconductor"` (từ keyword list + entity lookup)
> 3. Với mỗi content token → query Neo4j theo `surface`
> 4. Không tạo bất kỳ node nào trong Neo4j

### Knowledge Graph Nodes (đã có sẵn trong Neo4j)

#### Lexeme Nodes
```cypher
(:Lexeme {
  lexemeId: "lex_handsantai",
  surface: "半導体",
  reading: "はんどうたい",
  pos: "n",
  jlpt: 2,
  freq: 450,
  normalizedSurface: "半導体"
})

(:Lexeme {
  lexemeId: "lex_seizouk",
  surface: "製造",
  reading: "せいぞう",
  pos: "n",
  jlpt: 2
})

(:Lexeme {
  lexemeId: "lex_process",
  surface: "プロセス",
  reading: "プロセス",
  pos: "n",
  jlpt: 3
})

(:Lexeme {
  lexemeId: "lex_bimisai",
  surface: "微細",
  reading: "びさい",
  pos: "adj",
  jlpt: 3
})

(:Lexeme {
  lexemeId: "lex_kekka",
  surface: "欠陥",
  reading: "けっかん",
  pos: "n",
  jlpt: 2
})

(:Lexeme {
  lexemeId: "lex_kinou",
  surface: "性能",
  reading: "せいのう",
  pos: "n",
  jlpt: 2,
  freq: 580
})
```

#### Sense Nodes
```cypher
(:Sense {
  senseId: "sense_semiconductor_tech",
  glossVi: "bán dẫn",
  glossEn: "semiconductor",
  definition: "Vật liệu hoặc linh kiện có độ dẫn điện trung gian",
  domain: "technology",
  register: "technical",
  confidenceBase: 0.95,
  usageNote: "Chỉ các vật liệu hoặc linh kiện điện tử"
})

(:Sense {
  senseId: "sense_seiz_manufacture",
  glossVi: "sản xuất",
  glossEn: "manufacture",
  definition: "Quá trình tạo ra sản phẩm từ nguyên liệu",
  domain: "general",
  register: "technical",
  confidenceBase: 0.98
})

(:Sense {
  senseId: "sense_process_tech",
  glossVi: "quy trình / tập hợp công đoạn",
  glossEn: "process",
  definition: "Chuỗi các bước hoặc hoạt động",
  domain: "technology",
  register: "technical",
  confidenceBase: 0.92
})

(:Sense {
  senseId: "sense_kekka_defect",
  glossVi: "khuyết tật / lỗi",
  glossEn: "defect",
  definition: "Vấn đề hoặc thiếu sót trong sản phẩm",
  domain: "technology",
  register: "technical",
  confidenceBase: 0.96
})

(:Sense {
  senseId: "sense_kinou_perf",
  glossVi: "hiệu năng",
  glossEn: "performance",
  definition: "Khả năng hoạt động, chỉ số hoạt động của thiết bị",
  domain: "technology",
  register: "technical",
  confidenceBase: 0.94
})
```

#### Domain Nodes
```cypher
(:Domain {
  domainId: "domain_technology",
  name: "technology",
  description: "Lĩnh vực công nghệ, điện tử, máy tính"
})

(:Domain {
  domainId: "domain_semiconductor",
  name: "semiconductor",
  description: "Ngành công nghiệp bán dẫn"
})

(:Domain {
  domainId: "domain_manufacturing",
  name: "manufacturing",
  description: "Lĩnh vực sản xuất"
})
```

#### Register Nodes
```cypher
(:Register {
  registerId: "reg_technical",
  name: "technical",
  description: "Dùng trong ngữ cảnh kỹ thuật chuyên ngành"
})
```

#### Entity Nodes
```cypher
(:Entity {
  entityId: "ent_semiconductor",
  name: "Semiconductor",
  type: "Concept",
  description: "Vật liệu/linh kiện điện tử có độ dẫn điện trung gian",
  domain: "technology"
})

(:Entity {
  entityId: "ent_manufacturing_process",
  name: "Manufacturing Process",
  type: "Process",
  description: "Quy trình sản xuất công nghiệp",
  domain: "manufacturing"
})

(:Entity {
  entityId: "ent_defect",
  name: "Defect",
  type: "Concept",
  description: "Khuyết tật / lỗi trong sản phẩm",
  domain: "technology"
})

(:Entity {
  entityId: "ent_performance",
  name: "Performance",
  type: "Concept",
  description: "Chỉ số hiệu năng, khả năng hoạt động",
  domain: "technology"
})
```

#### Cue Nodes
```cypher
(:Cue {
  cueId: "cue_seiz",
  surface: "製造",
  category: "verb_companion",
  weight: 0.85
})

(:Cue {
  cueId: "cue_process",
  surface: "プロセス",
  category: "noun_collocate",
  weight: 0.88
})

(:Cue {
  cueId: "cue_kekka",
  surface: "欠陥",
  category: "technical_term",
  weight: 0.9
})

(:Cue {
  cueId: "cue_kinou",
  surface: "性能",
  category: "technical_term",
  weight: 0.92
})

(:Cue {
  cueId: "cueEig",
  surface: "影響",
  category: "action_verb",
  weight: 0.75
})
```

#### Example Nodes
```cypher
(:Example {
  exampleId: "ex_semiconductor_1",
  ja: "半導体チップの製造は非常に精密な作業である。",
  vi: "Sản xuất chip bán dẫn là công việc rất tinh xảo.",
  en: "Manufacturing semiconductor chips is a very precise task.",
  source: "corpus",
  qualityScore: 0.95,
  domain: "semiconductor"
})

(:Example {
  exampleId: "ex_defect_1",
  ja: "微細な欠陥でも電子回路に大きな悪影響を及ぼす可能性がある。",
  vi: "Thậm chí những khuyết tật nhỏ cũng có thể gây ảnh hưởng xấu lớn đến mạch điện tử.",
  source: "corpus",
  qualityScore: 0.93,
  domain: "semiconductor"
})

(:Example {
  exampleId: "ex_performance_1",
  ja: "チップの性能はプロセスノードに依存する。",
  vi: "Hiệu năng của chip phụ thuộc vào quy trình sản xuất.",
  source: "corpus",
  qualityScore: 0.92,
  domain: "semiconductor"
})
```

#### Relationships (knowledge graph only)
```cypher
# Lexeme -> Sense
(lex_handsantai)-[:HAS_SENSE]->(sense_semiconductor_tech)
(lex_seiz)-[:HAS_SENSE]->(sense_seiz_manufacture)
(lex_process)-[:HAS_SENSE]->(sense_process_tech)
(lex_kekka)-[:HAS_SENSE]->(sense_kekka_defect)
(lex_kinou)-[:HAS_SENSE]->(sense_kinou_perf)

# Sense -> Domain
(sense_semiconductor_tech)-[:BELONGS_TO {primary: true}]->(domain_semiconductor)
(sense_seiz_manufacture)-[:BELONGS_TO]->(domain_manufacturing)
(sense_process_tech)-[:BELONGS_TO]->(domain_technology)
(sense_kekka_defect)-[:BELONGS_TO]->(domain_semiconductor)
(sense_kinou_perf)-[:BELONGS_TO]->(domain_semiconductor)

# Sense -> Register
(sense_semiconductor_tech)-[:HAS_REGISTER]->(reg_technical)
(sense_seiz_manufacture)-[:HAS_REGISTER]->(reg_technical)
(sense_process_tech)-[:HAS_REGISTER]->(reg_technical)
(sense_kekka_defect)-[:HAS_REGISTER]->(reg_technical)
(sense_kinou_perf)-[:HAS_REGISTER]->(reg_technical)

# Sense -> Entity
(sense_semiconductor_tech)-[:REFERS_TO]->(ent_semiconductor)
(sense_seiz_manufacture)-[:REFERS_TO]->(ent_manufacturing_process)
(sense_process_tech)-[:REFERS_TO]->(ent_manufacturing_process)
(sense_kekka_defect)-[:REFERS_TO]->(ent_defect)
(sense_kinou_perf)-[:REFERS_TO]->(ent_performance)

# Sense -> Cue
(sense_semiconductor_tech)-[:SUPPORTED_BY {strength: 0.85}]->(cue_seiz)
(sense_semiconductor_tech)-[:SUPPORTED_BY {strength: 0.88}]->(cue_process)
(sense_kekka_defect)-[:SUPPORTED_BY {strength: 0.9}]->(cue_kekka)
(sense_kinou_perf)-[:SUPPORTED_BY {strength: 0.92}]->(cue_kinou)

# Sense -> Example
(sense_semiconductor_tech)-[:HAS_EXAMPLE]->(ex_semiconductor_1)
(sense_kekka_defect)-[:HAS_EXAMPLE]->(ex_defect_1)
(sense_kinou_perf)-[:HAS_EXAMPLE]->(ex_performance_1)

# Example -> Domain
(ex_semiconductor_1)-[:IN_DOMAIN]->(domain_semiconductor)
(ex_defect_1)-[:IN_DOMAIN]->(domain_semiconductor)
(ex_performance_1)-[:IN_DOMAIN]->(domain_semiconductor)
```

> **Lưu ý:** Không có Token/Sentence node nào trong graph. Mọi relationship trên đều là knowledge tĩnh.

---

## 5. Query Patterns cho Deep Analysis

### 5.1 Sense Disambiguation theo surface (Stateless)
Query trực tiếp theo `surface` của token — không cần Sentence node:

```cypher
// $tokenSurface = surface của token (e.g. "半導体")
// $detectedDomains = list domain phát hiện từ input (e.g. ["technology", "semiconductor"])
MATCH (lex:Lexeme {surface: $tokenSurface})
  -[:HAS_SENSE]->(sense:Sense)
OPTIONAL MATCH (sense)-[:BELONGS_TO]->(d:Domain)
OPTIONAL MATCH (sense)-[:SUPPORTED_BY]->(c:Cue)
OPTIONAL MATCH (sense)-[:HAS_EXAMPLE]->(ex:Example)
OPTIONAL MATCH (sense)-[:HAS_REGISTER]->(reg:Register)
OPTIONAL MATCH (sense)-[:REFERS_TO]->(ent:Entity)
RETURN {
  lexeme: lex.surface,
  senses: collect(DISTINCT {
    senseId: sense.senseId,
    glossVi: sense.glossVi,
    glossEn: sense.glossEn,
    domain: d.name,
    register: reg.name,
    entity: ent.name,
    cues: collect(DISTINCT c.surface),
    examples: collect(DISTINCT ex.vi),
    confidence: sense.confidenceBase
  })
}
```

### 5.2 Domain-Aware Retrieval (Stateless)
Lọc sense theo domain phát hiện từ in-memory analysis:

```cypher
// $tokenSurface = surface token
// $detectedDomains = list domain phát hiện (in-memory)
MATCH (lex:Lexeme {surface: $tokenSurface})
  -[:HAS_SENSE]->(sense:Sense)
  -[:BELONGS_TO]->(senseDomain:Domain)
WHERE senseDomain.name IN $detectedDomains
   OR NOT EXISTS((sense)-[:BELONGS_TO]->(:Domain))
OPTIONAL MATCH (sense)-[:HAS_EXAMPLE]->(ex:Example)
OPTIONAL MATCH (sense)-[:SUPPORTED_BY]->(cue:Cue)
WITH sense, ex, cue, senseDomain,
     CASE
       WHEN senseDomain.name IN $detectedDomains THEN 10
       WHEN cue IS NOT NULL THEN 8
       ELSE 5
     END AS scoreBoost
RETURN sense, ex, cue, scoreBoost
ORDER BY scoreBoost DESC
```

### 5.3 Collocation-Based Sense Ranking (Stateless)
Chấm điểm sense dựa trên collocation với các surface xung quanh (truyền từ in-memory):

```cypher
// $tokenSurface = surface của token hiện tại
// $neighborSurfaces = list surface các token lân cận (in-memory)
MATCH (lex:Lexeme {surface: $tokenSurface})
  -[:HAS_SENSE]->(sense:Sense)
OPTIONAL MATCH (sense)-[:HAS_COLLOCATION]->(coll:Collocation)
WHERE ANY(neighbor IN $neighborSurfaces WHERE coll.text CONTAINS neighbor)
RETURN sense, coll, coll.weight AS score
ORDER BY score DESC
```

### 5.4 Batch Retrieval cho nhiều token cùng lúc
Thay vì query từng token, batch query toàn bộ token của đoạn văn:

```cypher
// $tokenSurfaces = list tất cả surface content token (in-memory)
// $detectedDomains = list domain phát hiện
UNWIND $tokenSurfaces AS surface
MATCH (lex:Lexeme {surface: surface})
  -[:HAS_SENSE]->(sense:Sense)
OPTIONAL MATCH (sense)-[:BELONGS_TO]->(dom:Domain)
OPTIONAL MATCH (sense)-[:HAS_REGISTER]->(reg:Register)
OPTIONAL MATCH (sense)-[:SUPPORTED_BY]->(cue:Cue)
OPTIONAL MATCH (sense)-[:HAS_EXAMPLE]->(ex:Example)
OPTIONAL MATCH (sense)-[:REFERS_TO]->(ent:Entity)
WITH surface, lex, sense, dom, reg,
     collect(DISTINCT cue.surface) AS cues,
     collect(DISTINCT {ja: ex.ja, vi: ex.vi}) AS examples,
     ent
RETURN
  surface AS token,
  lex.reading AS reading,
  lex.jlpt AS jlpt,
  sense.senseId AS senseId,
  sense.glossVi AS glossVi,
  sense.definition AS definition,
  dom.name AS domain,
  reg.name AS register,
  ent.name AS entity,
  cues,
  examples,
  sense.confidenceBase AS baseConfidence
ORDER BY surface
```

---

## 6. Tiêu chuẩn Dữ liệu cho Dịch Chuyên Sâu

Để hệ thống dịch hoạt động tốt, tuân theo các chuẩn sau:

### 6.1 Lexeme & Sense Coverage
- ✅ Mỗi **Lexeme đa nghĩa** nên có ≥ 2 Sense
- ✅ Mỗi **Sense** trong domain chuyên ngành nên có:
  - Glossary tiếng Việt rõ ràng
  - Definition chi tiết
  - Assigned Domain và Register

### 6.2 Example Coverage
- ✅ Mỗi **Sense** nên có ≥ 5-10 Example tốt:
  - Bao phủ các cách sử dụng khác nhau
  - Quality Score ≥ 0.80
  - Có translation Việt chuẩn

### 6.3 Cue Coverage
- ✅ Mỗi **Sense** chuyên ngành nên có ≥ 10-20 Cue:
  - Collocation partners
  - Topic markers
  - Action verbs thường đi kèm
  - Weight biểu thị độ tin cậy

### 6.4 Domain & Register
- ✅ Mỗi **Sense** chuyên ngành phải có:
  - Assigned Domain (technology, medicine, culture, etc.)
  - Assigned Register (technical, formal, colloquial, archaic, etc.)
  - Entity linking (REFERS_TO)

### 6.5 Cultural Notes
- ✅ Các Sense liên quan văn hóa, idiom, formal speech phải có:
  - CulturalNote với explanation rõ
  - Linked từ Sense hoặc Entity

---

## 7. Hướng Dẫn Import CSV + Cypher

### 7.1 Chuẩn bị dữ liệu (CSV Format)

**domains.csv**
```
domainId,name,description
domain_technology,technology,Lĩnh vực công nghệ
domain_medicine,medicine,Lĩnh vực y tế
domain_culture,culture,Lĩnh vực văn hóa
```

**registers.csv**
```
registerId,name,description
reg_technical,technical,Dùng trong ngữ cảnh kỹ thuật
reg_formal,formal,Văn phong chính thức
reg_colloquial,colloquial,Văn nói thông thường
```

**entities.csv**
```
entityId,name,type,description,domain
ent_semiconductor,Semiconductor,Concept,Vật liệu điện tử,domain_technology
ent_disease,Disease,Concept,Bệnh tật,domain_medicine
```

**lexemes.csv**
```
lexemeId,surface,reading,pos,jlpt,freq,source,normalizedSurface
lex_handsantai,半導体,はんどうたい,n,2,450,jmdict,半導体
lex_byouki,病気,びょうき,n,2,300,jmdict,病気
```

**senses.csv**
```
senseId,glossVi,glossEn,definition,domain,register,confidenceBase,usageNote
sense_semiconductor_tech,bán dẫn,semiconductor,Vật liệu có độ dẫn điện trung gian,domain_technology,reg_technical,0.95,Chỉ vật liệu/linh kiện
sense_byouki_disease,bệnh,disease,Tình trạng không khỏe mạnh,domain_medicine,reg_formal,0.98,Chỉ bệnh tật
```

**examples.csv**
```
exampleId,ja,vi,en,source,qualityScore,domain
ex_semiconductor_1,半導体チップ,chip bán dẫn,semiconductor chip,corpus,0.95,domain_technology
ex_byouki_1,彼は重い病気にかかった,Anh ta bị ốm nặng,He caught a serious disease,corpus,0.93,domain_medicine
```

**cues.csv**
```
cueId,surface,category,weight
cue_seiz,製造,verb_companion,0.85
cue_byouki,重い,adjective_intensifier,0.82
```

### 7.2 Cypher Import Script

```cypher
// 1. Load Domain
LOAD CSV WITH HEADERS FROM "file:///domains.csv" AS row
CREATE (d:Domain {
  domainId: row.domainId,
  name: row.name,
  description: row.description
});
CREATE INDEX ON :Domain(domainId);

// 2. Load Register
LOAD CSV WITH HEADERS FROM "file:///registers.csv" AS row
CREATE (r:Register {
  registerId: row.registerId,
  name: row.name,
  description: row.description
});
CREATE INDEX ON :Register(registerId);

// 3. Load Entity (depends on Domain)
LOAD CSV WITH HEADERS FROM "file:///entities.csv" AS row
MATCH (d:Domain {domainId: row.domain})
CREATE (e:Entity {
  entityId: row.entityId,
  name: row.name,
  type: row.type,
  description: row.description,
  domain: row.domain
})-[:BELONGS_TO]->(d);
CREATE INDEX ON :Entity(entityId);

// 4. Load Lexeme
LOAD CSV WITH HEADERS FROM "file:///lexemes.csv" AS row
CREATE (l:Lexeme {
  lexemeId: row.lexemeId,
  surface: row.surface,
  reading: row.reading,
  pos: row.pos,
  jlpt: toInteger(row.jlpt),
  freq: toInteger(row.freq),
  source: row.source,
  normalizedSurface: row.normalizedSurface
});
CREATE INDEX ON :Lexeme(lexemeId);
CREATE INDEX ON :Lexeme(surface);

// 5. Load Sense (depends on Lexeme, Domain, Register)
LOAD CSV WITH HEADERS FROM "file:///senses.csv" AS row
MATCH (d:Domain {domainId: row.domain})
MATCH (r:Register {registerId: row.register})
CREATE (s:Sense {
  senseId: row.senseId,
  glossVi: row.glossVi,
  glossEn: row.glossEn,
  definition: row.definition,
  domain: row.domain,
  register: row.register,
  confidenceBase: toFloat(row.confidenceBase),
  usageNote: row.usageNote
})-[:BELONGS_TO]->(d)
(s)-[:HAS_REGISTER]->(r);
CREATE INDEX ON :Sense(senseId);

// 6. Link Sense -[:REFERS_TO]-> Entity
LOAD CSV WITH HEADERS FROM "file:///sense_entity_mapping.csv" AS row
MATCH (s:Sense {senseId: row.senseId})
MATCH (e:Entity {entityId: row.entityId})
CREATE (s)-[:REFERS_TO]->(e);

// 7. Load Cue
LOAD CSV WITH HEADERS FROM "file:///cues.csv" AS row
CREATE (c:Cue {
  cueId: row.cueId,
  surface: row.surface,
  category: row.category,
  weight: toFloat(row.weight)
});
CREATE INDEX ON :Cue(cueId);

// 8. Link Sense -[:SUPPORTED_BY]-> Cue
LOAD CSV WITH HEADERS FROM "file:///sense_cue_mapping.csv" AS row
MATCH (s:Sense {senseId: row.senseId})
MATCH (c:Cue {cueId: row.cueId})
CREATE (s)-[:SUPPORTED_BY {strength: toFloat(row.strength)}]->(c);

// 9. Load Example
LOAD CSV WITH HEADERS FROM "file:///examples.csv" AS row
CREATE (ex:Example {
  exampleId: row.exampleId,
  ja: row.ja,
  vi: row.vi,
  en: row.en,
  source: row.source,
  qualityScore: toFloat(row.qualityScore),
  domain: row.domain
});
CREATE INDEX ON :Example(exampleId);

// 10. Link Sense -[:HAS_EXAMPLE]-> Example
LOAD CSV WITH HEADERS FROM "file:///sense_example_mapping.csv" AS row
MATCH (s:Sense {senseId: row.senseId})
MATCH (ex:Example {exampleId: row.exampleId})
CREATE (s)-[:HAS_EXAMPLE]->(ex);

// 11. Link Lexeme -[:HAS_SENSE]-> Sense
LOAD CSV WITH HEADERS FROM "file:///lexeme_sense_mapping.csv" AS row
MATCH (l:Lexeme {lexemeId: row.lexemeId})
MATCH (s:Sense {senseId: row.senseId})
CREATE (l)-[:HAS_SENSE]->(s);

// Create index for fast query
CREATE INDEX ON :Sentence(sentenceId);
CREATE INDEX ON :Token(tokenId);
```

---

## 8. Best Practices

### 8.1 Design & Maintenance
- **Denormalization khi cần:** Nếu query thường cần domain name, store trực tiếp trong node (domain: String)
- **Weight & Confidence:** Luôn assign confidence/weight cho relationship và node
- **Source tracking:** Lưu source (jmdict, corpus, user) để audit & validation

### 8.2 Query Performance
- **Index chiến lược:** Domain, Register, Entity, Sense by ID
- **Batch retrieval:** Lấy tất cả sense của token cùng lúc thay vì từng cái
- **Limit depth:** Không traverse quá 3-4 levels trong một query

### 8.3 LLM Integration
- **Structured output:** Return JSON từ query để dễ parse
- **Fallback handling:** Nếu không có Cue/Example, vẫn return sense + definition
- **Confidence filtering:** Chỉ return sense với confidence ≥ threshold

---

## 9. Cơ cấu Folder Dữ liệu (Suggested)

```
neo4j-data/
├── csv/
│   ├── domains.csv
│   ├── registers.csv
│   ├── entities.csv
│   ├── lexemes.csv
│   ├── senses.csv
│   ├── examples.csv
│   ├── cues.csv
│   ├── collocations.csv
│   ├── grammar_patterns.csv
│   ├── cultural_notes.csv
│   ├── sense_entity_mapping.csv
│   ├── sense_cue_mapping.csv
│   ├── sense_example_mapping.csv
│   ├── sense_collocation_mapping.csv
│   ├── lexeme_sense_mapping.csv
│   └── token_sense_mapping.csv
├── cypher/
│   ├── 00_create_indexes.cypher
│   ├── 01_load_domain_register.cypher
│   ├── 02_load_entities.cypher
│   ├── 03_load_lexemes.cypher
│   ├── 04_load_senses.cypher
│   ├── 05_load_examples_cues.cypher
│   ├── 06_build_relationships.cypher
│   ├── 07_query_patterns.cypher
│   └── README.md
└── README.md
```

---

## 10. Tóm Tắt

| Yếu tố | Lưu Neo4j | Tác dụng | Lưu ý |
|--------|-----------|---------|-------|
| **Lexeme** | ✅ | Từ bề mặt | Deduplication bằng normalizedSurface |
| **Sense** | ✅ | Nghĩa cụ thể | 1 Lexeme có thể nhiều Sense |
| **Domain** | ✅ | Miền ngữ cảnh | Quan trọng cho WSD & translation |
| **Register** | ✅ | Sắc thái sử dụng | Technical/formal/colloquial |
| **Cue** | ✅ | Tín hiệu phân biệt | Weight = confidence |
| **Collocation** | ✅ | Cụm từ đi kèm | glossVi của cụm |
| **Example** | ✅ | Ví dụ đã dịch | ≥ 5-10 per Sense tốt |
| **Entity** | ✅ | Khái niệm tri thức | Link Sense via REFERS_TO |
| **CulturalNote** | ✅ | Ghi chú văn hóa | Linked từ Sense hoặc Entity |
| **GrammarPattern** | ✅ | Mẫu ngữ pháp | Dùng cho câu học thuật |
| **Token** | ❌ | Từ tách từ input | In-memory (MeCab/sudachi) |
| **Sentence** | ❌ | Câu đầu vào | In-memory; lịch sử lưu PostgreSQL |

**Thứ tự import:** Domain → Register → Entity → Lexeme → Sense → Cue/Collocation/Example/CulturalNote → Relationships

**Mục tiêu:** Graph đủ rich để LLM lấy context & evidence cho dịch chính xác + tự tin. Query stateless theo surface.

---

## 11. Pipeline GraphRAG + LLM Cho Dịch Đoạn Văn

Pipeline này dùng cho đoạn văn có các câu chứa thực thể thuộc miền học tập, văn hóa, hoặc chuyên ngành, nơi dịch máy thường sai sắc thái, sai register, hoặc dịch quá literal.

### 11.1 Mục tiêu
- Giữ đúng nghĩa từ vựng đa nghĩa theo ngữ cảnh.
- Ưu tiên đúng domain, register, collocation, và cultural nuance.
- Trả ra bản dịch tự nhiên, nhưng vẫn bám chặt evidence từ graph.

### 11.2 Luồng xử lý tổng quát (Stateless)

```text
Input đoạn văn
  -> [In-memory] Tách câu
  -> [In-memory] Tokenize (MeCab) → list surface
  -> [In-memory] Detect domain/entity từ keyword
  -> [Neo4j] Batch query theo list surface + detected domains
  -> [In-memory] Rank sense theo scoring formula
  -> [In-memory] Build structured prompt
  -> [LLM] Dịch và sinh notes
  -> [In-memory] Post-check consistency
  -> Output: translation + keyVocabulary + notes
```

> **Không có bước write vào Neo4j.** Toàn bộ pipeline là read-only với graph.

### 11.3 Các bước chi tiết

#### Bước 1: Sentence segmentation (in-memory)
- Chia đoạn văn thành từng câu.
- Giữ in-memory dưới dạng `List<String>` — không persist.

#### Bước 2: Tokenization và entity/domain detection (in-memory)
- Tách token bằng MeCab/sudachi → `List<Token(surface, reading, pos, position)>`.
- Lọc content token (bỏ particle, punctuation).
- Detect domain từ keyword list hoặc entity lookup nhanh trên Neo4j.

#### Bước 3: Graph retrieval (Neo4j batch query)
Truy vấn một lần cho toàn bộ token của đoạn:

```cypher
// Dùng query 5.4 (Batch Retrieval)
UNWIND $tokenSurfaces AS surface
MATCH (lex:Lexeme {surface: surface})
  -[:HAS_SENSE]->(sense:Sense)
OPTIONAL MATCH (sense)-[:BELONGS_TO]->(dom:Domain)
OPTIONAL MATCH (sense)-[:HAS_REGISTER]->(reg:Register)
OPTIONAL MATCH (sense)-[:SUPPORTED_BY]->(cue:Cue)
OPTIONAL MATCH (sense)-[:HAS_EXAMPLE]->(ex:Example)
OPTIONAL MATCH (sense)-[:REFERS_TO]->(ent:Entity)
RETURN surface, lex, sense, dom, reg,
  collect(DISTINCT cue.surface) AS cues,
  collect(DISTINCT {ja: ex.ja, vi: ex.vi}) AS examples,
  ent
```

#### Bước 4: Sense ranking
Điểm cuối cho mỗi sense có thể tính theo:

$$
score = w_d \cdot domainMatch + w_r \cdot registerMatch + w_c \cdot cueMatch + w_l \cdot collocationMatch + w_e \cdot exampleMatch + w_b \cdot confidenceBase
$$

Gợi ý trọng số:
- `w_d = 0.30`
- `w_r = 0.15`
- `w_c = 0.25`
- `w_l = 0.15`
- `w_e = 0.10`
- `w_b = 0.05`

#### Bước 5: Prompt assembly
Prompt cho LLM nên gồm 4 phần:
- `source_text`: câu gốc hoặc đoạn gốc
- `top_senses`: top sense theo từng token quan trọng
- `evidence`: cue, example, entity, domain, register
- `translation_constraints`: yêu cầu giữ tự nhiên, không dịch literal, không bỏ register/cultural nuance

Ví dụ cấu trúc prompt:

```text
Bạn là dịch giả chuyên ngành.
Nhiệm vụ: dịch đoạn văn tiếng Nhật sang tiếng Việt.
Yêu cầu:
- Giữ đúng domain và register.
- Ưu tiên nghĩa theo evidence từ graph.
- Nếu từ đa nghĩa, dùng sense có score cao nhất.
- Nếu câu có sắc thái văn hóa, diễn đạt tự nhiên bằng tiếng Việt, không dịch cứng.

Source text:
<đoạn văn>

Graph evidence:
- Token: 半導体 -> Sense: bán dẫn | Domain: technology | Register: technical
- Token: 欠陥 -> Sense: khuyết tật | Cue: 微細
- Token: 配慮 -> Sense: sự tinh tế / để ý đến người khác | Domain: culture

Output:
1) Bản dịch tự nhiên
2) Ghi chú ngắn nếu có chỗ cần giải thích sắc thái
```

#### Bước 6: Translation generation
- LLM tạo bản dịch câu theo câu.
- Với đoạn dài, nên dịch từng câu rồi hợp nhất để giảm drift.
- Nếu có thuật ngữ chuyên ngành, giữ nhất quán toàn đoạn.

#### Bước 7: Post-check
Kiểm tra hậu kỳ bằng rule-based hoặc LLM phụ:
- Thuật ngữ có nhất quán không.
- Sắc thái văn hóa có bị làm cứng hoặc quá khẩu ngữ không.
- Có token nào bị bỏ sót không.
- Có câu nào quá literal không.

#### Bước 8: Final output
Output nên có 2 lớp:
- `translation`: bản dịch cuối
- `notes`: ghi chú ngắn về từ đa nghĩa, văn hóa, hoặc quyết định dịch quan trọng

### 11.4 Chiến lược cho từng loại câu

#### Câu chuyên ngành
- Ưu tiên `Domain`, `Register`, `Cue`, `Example`.
- Dùng entity để khóa nghĩa.
- Không ưu tiên translation phổ thông nếu cue cho thấy ngữ cảnh kỹ thuật.

#### Câu văn hóa / lễ nghi
- Ưu tiên `CulturalNote`, `Entity`, `Register`.
- LLM nên diễn đạt tự nhiên theo văn phong tiếng Việt, không dịch sát từng từ nếu làm mất sắc thái.

#### Câu học thuật / giáo dục
- Ưu tiên `Domain` học tập, `GrammarPattern`, `Example`.
- Dịch đúng cấu trúc học thuật, tránh khẩu ngữ hóa.

### 11.5 Output format khuyến nghị cho backend

```json
{
  "translation": "Trong quy trình sản xuất bán dẫn, các khuyết tật vi mô có ảnh hưởng lớn đến hiệu năng.",
  "keyVocabulary": [
    {
      "surface": "半導体",
      "reading": "はんどうたい",
      "jlpt": 2,
      "glossVi": "bán dẫn",
      "domain": "technology",
      "register": "technical"
    },
    {
      "surface": "欠陥",
      "reading": "けっかん",
      "jlpt": 2,
      "glossVi": "khuyết tật / lỗi",
      "domain": "technology",
      "register": "technical"
    }
  ],
  "notes": [
    { "type": "polysemy", "token": "性能", "content": "Dịch là 'hiệu năng' theo ngữ cảnh kỹ thuật, không phải 'tính năng' chung." },
    { "type": "technical", "token": "製造プロセス", "content": "Collocation chuyên ngành bán dẫn: 'quy trình sản xuất'." }
  ]
}
```

> **Lưu ý:** Không có `sentenceId` trong output vì Sentence không persist. Nếu cần lưu lịch sử, backend lưu toàn bộ object này vào PostgreSQL.

### 11.6 Kết luận pipeline
- **Neo4j** chỉ lưu knowledge tĩnh, read-only trong mọi request.
- **Application layer** xử lý tokenization, domain detection, sense ranking hoàn toàn in-memory.
- **LLM** nhận structured prompt với evidence từ graph để dịch tự nhiên và chính xác.
- **PostgreSQL** lưu lịch sử bản dịch nếu cần (tùy tính năng).

