"""
SocialLens - Real-World Dataset Preparation & Formatting Tool
==============================================================
Helps students and researchers convert, clean, and validate real-world social media
datasets (Kaggle, Reddit, Twitter, Bluesky, research dumps) for direct upload into SocialLens.

Usage Examples:
1. Convert an external Kaggle CSV:
   python prepare_dataset.py convert --input external_tweets.csv --output my_dataset.csv --text-col tweet_text --date-col created_at

2. Generate a 120-post realistic multi-day benchmark dataset:
   python prepare_dataset.py generate --output realworld_tech_benchmark.csv --count 120

3. Validate a CSV before upload:
   python prepare_dataset.py validate --input my_dataset.csv
"""

import argparse
import csv
import datetime
import random
import sys
from dateutil import parser as dt_parser

def convert_dataset(input_path, output_path, text_col, date_col=None, author_col=None, platform_col=None, like_col=None, reply_col=None, share_col=None, reply_to_col=None):
    print(f"Reading {input_path}...")
    with open(input_path, mode="r", encoding="utf-8", errors="replace") as infile:
        reader = csv.DictReader(infile)
        rows = list(reader)

    if not rows:
        print("Error: Input CSV is empty!")
        return

    fieldnames = ["post_id", "platform", "author_id", "text", "created_at", "like_count", "reply_count", "share_count", "reply_to_post_id"]
    output_rows = []

    base_time = datetime.datetime.now() - datetime.timedelta(days=7)

    for idx, r in enumerate(rows, 1):
        # Extract text (required)
        text = r.get(text_col, "").strip() if text_col in r else ""
        if not text:
            continue

        # Extract or synthesize timestamp
        created_at_str = ""
        if date_col and date_col in r and r[date_col]:
            try:
                parsed_dt = dt_parser.parse(r[date_col])
                created_at_str = parsed_dt.strftime("%Y-%m-%d %H:%M:%S")
            except Exception:
                created_at_str = (base_time + datetime.timedelta(hours=idx * 2)).strftime("%Y-%m-%d %H:%M:%S")
        else:
            created_at_str = (base_time + datetime.timedelta(hours=idx * 2)).strftime("%Y-%m-%d %H:%M:%S")

        # Author
        author_id = r.get(author_col, "").strip() if author_col and author_col in r else f"user_{idx % 40 + 1}"
        if not author_id:
            author_id = f"user_{idx % 40 + 1}"

        # Platform
        platform = r.get(platform_col, "TWITTER").strip().upper() if platform_col and platform_col in r else "TWITTER"

        # Counts
        def parse_int(val, default=0):
            try:
                return int(float(val))
            except Exception:
                return default

        likes = parse_int(r.get(like_col, 0)) if like_col and like_col in r else random.randint(0, 50)
        replies = parse_int(r.get(reply_col, 0)) if reply_col and reply_col in r else random.randint(0, 10)
        shares = parse_int(r.get(share_col, 0)) if share_col and share_col in r else random.randint(0, 15)
        reply_to = r.get(reply_to_col, "").strip() if reply_to_col and reply_to_col in r else ""

        output_rows.append({
            "post_id": f"post_{idx}",
            "platform": platform,
            "author_id": author_id,
            "text": text,
            "created_at": created_at_str,
            "like_count": likes,
            "reply_count": replies,
            "share_count": shares,
            "reply_to_post_id": reply_to
        })

    with open(output_path, mode="w", newline="", encoding="utf-8") as outfile:
        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(output_rows)

    print(f"Successfully converted {len(output_rows)} records into: {output_path}")

def validate_dataset(input_path):
    print(f"Validating {input_path}...")
    with open(input_path, mode="r", encoding="utf-8", errors="replace") as infile:
        reader = csv.DictReader(infile)
        rows = list(reader)

    if not rows:
        print("Error: CSV file is empty.")
        return False

    headers = reader.fieldnames or []
    print(f"Detected columns: {headers}")

    text_found = any(h.lower() in ["text", "posttext", "tweet", "content", "body"] for h in headers)
    if not text_found:
        print("Warning: No standard text column detected (expected 'text', 'tweet', 'content', or 'body').")
    else:
        print("Text column detected.")

    date_found = any(h.lower() in ["created_at", "date", "timestamp", "time"] for h in headers)
    if not date_found:
        print("Warning: No timestamp column detected. Temporal anomaly detection will be limited.")
    else:
        print("Timestamp column detected.")

    valid_text_count = sum(1 for r in rows if any(r.get(h, "").strip() for h in headers if "text" in h.lower() or "content" in h.lower()))
    print(f"Total rows: {len(rows)}, Non-empty text rows: {valid_text_count}")
    return True

def generate_benchmark_dataset(output_path, count=120):
    print(f"Generating realistic multi-day research benchmark ({count} posts)...")
    
    topics = [
        # Topic 1: Autonomous AI Agents & Coding
        {
            "category": "AI Coding Agents",
            "pos": [
                "The new AI autonomous coding agent resolved 15 complex bugs in 5 minutes! Unbelievable productivity boost.",
                "Using autonomous AI agents for fullstack refactoring is magical. Architecture diagrams and unit tests generated flawlessly.",
                "Incredible work on the multi-agent orchestration framework. Parallel tool calling works like a charm!",
                "AI assisted development is saving our team 20 hours a week. Best developer experience ever.",
                "The new AI coding IDE feels like programming from the year 2035. Blown away by the speed."
            ],
            "neg": [
                "The coding agent deleted my entire database schema during a refactoring pass! Completely catastrophic failure.",
                "AI hallucinations in unit tests are getting worse. It invented 10 non-existent library methods and passed fake assertions.",
                "Autonomous agent got stuck in an infinite retry loop and consumed $150 of API credits in 10 minutes. Terrible safeguard design.",
                "The generated code has severe security vulnerabilities and SQL injection flaws. Completely unusable in production."
            ],
            "neu": [
                "Comparing benchmark results between multi-agent coding frameworks on HumanEval dataset.",
                "Has anyone tried integrating local LLM weights into the new agentic IDE setup?",
                "Autonomous agent architecture paper published on arXiv today covering self-reflection loops.",
                "Looking for documentation on how to configure tool calling timeouts in the Python SDK."
            ]
        },
        # Topic 2: Electric Vehicle Battery & Charging Infrastructure
        {
            "category": "EV Infrastructure",
            "pos": [
                "New solid-state EV battery hit 800 miles range on a single 15-minute charge! The future is finally here.",
                "Supercharger network expansion reached our rural highway corridor today. Seamless road trip experience!",
                "Battery degradation after 100,000 miles is only 2.5%. Impressive engineering and thermal management.",
                "Loving the bi-directional vehicle-to-home charging feature during power outages."
            ],
            "neg": [
                "Arrived at the highway charging station and 6 out of 8 stalls were broken! Stuck with 3% battery in freezing rain.",
                "Winter range drops by over 45% when heating is active. Completely impractical for cold climates.",
                "Customer support refused warranty replacement for failed battery module. Quoted $18,000 repair cost. Outrageous scam!",
                "Charging speeds throttled to 20kW after 10 minutes due to thermal runaway alert. Extremely frustrating."
            ],
            "neu": [
                "Evaluating the kilowatt-hour charging curve across 400V vs 800V vehicle architectures.",
                "Department of Transportation released new standards for national EV charging interoperability today.",
                "Battery chemistry comparison: LFP vs NMC energy density trade-offs for urban vehicles."
            ]
        },
        # Topic 3: Cyber Security & Zero-Day Exploit Burst
        {
            "category": "Cybersecurity Zero-Day",
            "pos": [
                "Security researchers rapidly patched the critical authentication bypass within 3 hours. Phenomenal response time!",
                "Great analysis by the threat intel team deconstructing the firmware exploit mechanism."
            ],
            "neg": [
                "CRITICAL ALERT: Zero-day remote code execution vulnerability actively exploited in widespread enterprise servers! 🚨",
                "Severe ransomware breach reported across 12 healthcare systems. Data exfiltration in progress! 🚨",
                "Emergency security advisory: Disconnect all unpatched gateways immediately! Active compromise detected.",
                "Our production cluster was compromised through the zero-day flaw. Data encrypted, disaster recovery initiated."
            ],
            "neu": [
                "CVE identifier assigned for the memory corruption vulnerability disclosed this morning.",
                "CISA added the remote execution flaw to the Known Exploited Vulnerabilities catalog."
            ]
        }
    ]

    # Pre-defined user pool for realistic network graph
    key_influencers = ["tech_analyst_prime", "ai_lead_researcher", "ev_guru_daily", "cyber_defense_center", "support_hero"]
    regular_users = [f"dev_user_{i}" for i in range(1, 35)]
    all_users = key_influencers + regular_users

    base_time = datetime.datetime.now() - datetime.timedelta(days=4)
    records = []

    # 1. Generate Organic Multi-Day Discussion
    for i in range(count - 25):
        topic = random.choice(topics)
        sentiment_pool = random.choices([topic["pos"], topic["neg"], topic["neu"]], weights=[0.38, 0.35, 0.27])[0]
        text = random.choice(sentiment_pool)
        
        # Add random hashtags or mentions
        if random.random() < 0.35:
            text += f" #{topic['category'].replace(' ', '')}"
        
        author = random.choice(all_users)
        reply_to = ""
        if random.random() < 0.25:
            reply_to = random.choice(key_influencers)
            text = f"@{reply_to} {text}"

        post_time = base_time + datetime.timedelta(
            hours=random.randint(0, 72),
            minutes=random.randint(0, 59),
            seconds=random.randint(0, 59)
        )

        records.append({
            "post_id": f"p_{1000 + i}",
            "platform": random.choice(["TWITTER", "TWITTER", "TWITTER", "REDDIT"]),
            "author_id": author,
            "text": text,
            "created_at": post_time.strftime("%Y-%m-%d %H:%M:%S"),
            "like_count": random.randint(5, 300) if author in key_influencers else random.randint(0, 45),
            "reply_count": random.randint(2, 60) if author in key_influencers else random.randint(0, 8),
            "share_count": random.randint(1, 40) if author in key_influencers else random.randint(0, 6),
            "reply_to_post_id": reply_to
        })

    # 2. Inject Bot Duplicate Spam (to test duplicate dampening)
    bot_spam_text = "Free crypto token airdrop claim now! Verify wallet at instant-claim-airdrop.xyz #Airdrop #Free"
    spam_time = base_time + datetime.timedelta(days=2, hours=10)
    for b in range(10):
        records.append({
            "post_id": f"spam_{2000 + b}",
            "platform": "TWITTER",
            "author_id": f"bot_spammer_{b + 1}",
            "text": bot_spam_text,
            "created_at": (spam_time + datetime.timedelta(minutes=b * 2)).strftime("%Y-%m-%d %H:%M:%S"),
            "like_count": random.randint(0, 2),
            "reply_count": 0,
            "share_count": 0,
            "reply_to_post_id": ""
        })

    # 3. Inject Emerging Anomaly Burst (sudden critical event surge)
    burst_time = base_time + datetime.timedelta(days=3, hours=14)
    burst_texts = [
        "Major cloud data center outage reported in US-East! 500+ services offline! 🚨",
        "AWS and Azure regional outage affecting thousands of enterprise portals simultaneously.",
        "Total network blackout across northern cloud region. Backup generators failed.",
        "All our production microservices are returning 504 gateway timeout due to the cloud backbone failure.",
        "Status page finally updated: Major power distribution failure at core internet exchange.",
        "Engineers on-site attempting to restore secondary power grid after massive blackout.",
        "Cloud outage enters hour 2. Severe financial losses reported across automated trading desks."
    ]
    for b_idx, b_text in enumerate(burst_texts):
        records.append({
            "post_id": f"burst_{3000 + b_idx}",
            "platform": "TWITTER",
            "author_id": random.choice(all_users),
            "text": b_text,
            "created_at": (burst_time + datetime.timedelta(minutes=b_idx * 6)).strftime("%Y-%m-%d %H:%M:%S"),
            "like_count": random.randint(80, 450),
            "reply_count": random.randint(20, 85),
            "share_count": random.randint(30, 110),
            "reply_to_post_id": "cyber_defense_center" if b_idx % 2 == 0 else ""
        })

    # Sort chronologically
    records.sort(key=lambda x: x["created_at"])

    # Write CSV
    fieldnames = ["post_id", "platform", "author_id", "text", "created_at", "like_count", "reply_count", "share_count", "reply_to_post_id"]
    with open(output_path, mode="w", newline="", encoding="utf-8") as outfile:
        writer = csv.DictWriter(outfile, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(records)

    print(f"Done! Created research benchmark with {len(records)} posts saved to: {output_path}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="SocialLens Real-World Dataset Utility")
    subparsers = parser.add_subparsers(dest="command")

    # Convert
    convert_parser = subparsers.add_parser("convert", help="Convert an external CSV to SocialLens format")
    convert_parser.add_argument("--input", required=True, help="Input CSV file path")
    convert_parser.add_argument("--output", required=True, help="Output formatted CSV file path")
    convert_parser.add_argument("--text-col", required=True, help="Name of column containing post text")
    convert_parser.add_argument("--date-col", help="Name of column containing post date/time")
    convert_parser.add_argument("--author-col", help="Name of column containing author/user")
    convert_parser.add_argument("--platform-col", help="Name of column containing platform")
    convert_parser.add_argument("--like-col", help="Name of column containing like count")
    convert_parser.add_argument("--reply-col", help="Name of column containing reply count")
    convert_parser.add_argument("--share-col", help="Name of column containing share count")
    convert_parser.add_argument("--reply-to-col", help="Name of column containing reply_to user/post")

    # Validate
    val_parser = subparsers.add_parser("validate", help="Validate a CSV for SocialLens compatibility")
    val_parser.add_argument("--input", required=True, help="CSV file path to validate")

    # Generate benchmark
    gen_parser = subparsers.add_parser("generate", help="Generate a realistic multi-day benchmark dataset")
    gen_parser.add_argument("--output", default="datasets/realworld_tech_benchmark.csv", help="Output file path")
    gen_parser.add_argument("--count", type=int, default=120, help="Number of records to generate")

    args = parser.parse_args()

    if args.command == "convert":
        convert_dataset(
            input_path=args.input,
            output_path=args.output,
            text_col=args.text_col,
            date_col=args.date_col,
            author_col=args.author_col,
            platform_col=args.platform_col,
            like_col=args.like_col,
            reply_col=args.reply_col,
            share_col=args.share_col,
            reply_to_col=args.reply_to_col
        )
    elif args.command == "validate":
        validate_dataset(args.input)
    elif args.command == "generate":
        generate_benchmark_dataset(args.output, count=args.count)
    else:
        parser.print_help()
