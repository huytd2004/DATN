# DATN — Project folder structure

**Generated:** 2026-05-14

Below is a concise, human-readable tree of the DATN workspace.

```
DATN/
├── ai/
│   └── langraph_pipeline/
│       ├── __init__.py
│       ├── config.py
│       ├── llm_client.py
│       ├── neo4j_client.py
│       ├── prompt_builder.py
│       ├── ranker.py
│       ├── README.md
│       ├── requirements.txt
│       └── runner.py
├── backend/
│   ├── datn_backup_20260415.sql
│   ├── HELP.md
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   │       └── java/
│   └── target/
│       ├── classes/
│       │   ├── application.yaml
│       │   └── vn/
│       ├── generated-sources/
│       │   └── annotations/
│       ├── generated-test-sources/
│       │   └── test-annotations/
│       └── maven-status/
│           └── maven-compiler-plugin/
├── crawl-data/
│   ├── datn_backup_20260415.sql
│   ├── flashcards_sample.json
│   ├── flashcards_sample.sql
│   ├── import_to_neo4j.py
│   ├── import_to_postgres.py
│   ├── migrate_schema.sql
│   ├── sample-data/ (helper scripts expect this)
│   └── env/
├── Mazii-crawler/
│   ├── crawler.py
│   ├── docker-compose.yml
│   ├── dockerfile
│   ├── general.py
│   ├── kanji_html.html
│   ├── link_finder.py
│   ├── main.py
│   ├── README.md
│   ├── requirements.txt
│   └── test_kanji.py
├── sample-data/
│   ├── 1-grammar.json
│   ├── 1-kanji.json
│   ├── 1.json
│   ├── 2-kanji.json
│   └── 2.json
├── docs/
│   ├── design.md  // thiết kế  ui-ux
│   ├── schema-neo4j.md 
│   ├── schema.md // schema cho postgresql
│   ├── sm2-algorithm.md 
│   └── summary.md  // kiến trúc tổng quan
├── frontend/
│   ├── index.html
│   ├── package.json
│   ├── README.md
│   ├── stitch.html
│   ├── vite.config.js
│   ├── public/
│   └── src/
│       ├── App.vue
│       ├── main.js
│       ├── style.css
│       ├── assets/
│       ├── components/
│       ├── layouts/
│       ├── router/
│       ├── services/
│       ├── stores/
│       └── views/
└── neo4j-data/
    └── csv/
        ├── lexeme_sense_mapping.csv
        ├── neo4j_data_cues.csv
        ├── neo4j_data_domains.csv
        ├── neo4j_data_entities.csv
        ├── neo4j_data_examples.csv
        ├── neo4j_data_lexemes.csv
        ├── neo4j_data_registers.csv
        ├── neo4j_data_senses.csv
        ├── neo4j_data_sentences.csv
        ├── neo4j_data_tokens.csv
        ├── sense_cue_mapping.csv
        ├── sense_entity_mapping.csv
        ├── sense_example_mapping.csv
        ├── sentence_domain_mapping.csv
        ├── sentence_entity_mapping.csv
        ├── sentence_token_mapping.csv
        ├── token_lexeme_mapping.csv
        └── token_sense_mapping.csv

```

If you want a full `tree` dump (including hidden files) or a JSON representation, tell me which format and I'll generate it.
