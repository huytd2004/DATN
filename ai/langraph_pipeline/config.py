import os
from dotenv import load_dotenv

load_dotenv()

NEO4J_URI = os.getenv('NEO4J_URI', 'bolt://localhost:7687')
NEO4J_USER = os.getenv('NEO4J_USER', 'neo4j')
NEO4J_PASS = os.getenv('NEO4J_PASS', '12345678')
NEO4J_DATABASE = os.getenv('NEO4J_DATABASE', 'specialized-graph')

OPENAI_API_KEY = os.getenv('OPENAI_API_KEY')
