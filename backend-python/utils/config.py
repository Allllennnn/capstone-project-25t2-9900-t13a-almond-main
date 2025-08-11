import os
from dotenv import load_dotenv

# Load environment variables from the `.env` file at the project root
load_dotenv()

# ========== OpenAI Configuration ==========

# Get OpenAI API Key (used to call GPT models)
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

# Raise error if OpenAI Key is not set
if not OPENAI_API_KEY:
    raise RuntimeError("Please set OPENAI_API_KEY in the .env file")

# ========== MySQL Database Configuration ==========

# Read database connection settings from .env
DB_HOST = os.getenv("DB_HOST")             # Database hostname or IP
DB_PORT = os.getenv("DB_PORT", "3306")     # Database port (default is 3306)
DB_NAME = os.getenv("DB_NAME")             # Database name
DB_USER = os.getenv("DB_USER")             # Username
DB_PASS = os.getenv("DB_PASS")             # Password

# Raise error if any required DB fields are missing
if not all([DB_HOST, DB_NAME, DB_USER, DB_PASS]):
    raise RuntimeError("Please set DB_HOST, DB_NAME, DB_USER, DB_PASS in the .env file")

# Construct SQLAlchemy async connection URL — using aiomysql driver
DATABASE_URL = (
    f"mysql+aiomysql://"
    f"{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
    "?charset=utf8mb4"
)
