# AntiProfanity
A simple plugin that detects profanity within the chat using AI.

## Configuration
```yaml
whitelisted-words: [] # List of words that are allowed (won't be flagged)
max-threads: -1 # Maximum number of threads to use for processing (default unlimited)
```

## API
This project uses [Profanity.Dev](https://profanity.dev) to detect profanity. They offer a free API that you can use to detect profanity in your chat. You can get your API key by signing up on their website.