FROM python:3.11

WORKDIR /app

# Install dependencies
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# Copy all Python code (including main.py)
COPY . .

# Expose port
EXPOSE 8000

# Startup command, directly run uvicorn
ENTRYPOINT ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
