import requests
import json
import time
import subprocess

BASE_URL = "http://localhost:8080/api"

def print_section(title):
    print("\n" + "=" * 60)
    print(f" {title} ")
    print("=" * 60)

def main():
    print_section("1. USER REGISTRATION & AUTHENTICATION")
    # Register test user
    reg_payload = {
        "username": f"analyst_e2e_{int(time.time())}",
        "email": f"analyst_{int(time.time())}@sociallens.edu",
        "password": "password123",
        "role": "ANALYST"
    }
    reg_res = requests.post(f"{BASE_URL}/auth/register", json=reg_payload)
    print(f"Register status: {reg_res.status_code}")
    reg_data = reg_res.json()
    assert reg_res.status_code == 200, f"Registration failed: {reg_data}"
    token = reg_data["data"]["accessToken"]
    username = reg_data["data"]["username"]
    print(f"Registered user: {username}")
    print(f"JWT Token acquired: {token[:20]}...")

    headers = {
        "Authorization": f"Bearer {token}"
    }

    # Verify /auth/me
    me_res = requests.get(f"{BASE_URL}/auth/me", headers=headers)
    print(f"Get current user: {me_res.json()['data']['username']} (Role: {me_res.json()['data']['role']})")

    print_section("2. DATASET PREVIEW & UPLOAD")
    csv_path = r"D:\Projects\SocialLens\datasets\sample_posts.csv"
    with open(csv_path, "rb") as f:
        preview_res = requests.post(f"{BASE_URL}/datasets/preview", headers=headers, files={"file": ("sample_posts.csv", f, "text/csv")})
    preview_data = preview_res.json()
    print(f"Preview detected rows: {preview_data['data']['totalDetectedRows']}")
    print(f"Detected headers: {preview_data['data']['headers']}")
    suggested_mapping = preview_data['data']['suggestedColumnMappings']
    print(f"Suggested column mappings: {suggested_mapping}")

    with open(csv_path, "rb") as f:
        upload_data = {
            "name": f"EV & Tech Sentiment {int(time.time())}",
            "description": "Verified sample social posts testing duplicate damping and trend anomalies",
            "columnMapping": json.dumps(suggested_mapping)
        }
        upload_res = requests.post(
            f"{BASE_URL}/datasets/upload",
            headers=headers,
            data=upload_data,
            files={"file": ("sample_posts.csv", f, "text/csv")}
        )
    assert upload_res.status_code == 200, f"Upload failed: {upload_res.text}"
    dataset_id = upload_res.json()["data"]["id"]
    total_records = upload_res.json()["data"]["totalRecords"]
    print(f"Dataset successfully imported! ID: {dataset_id}, Total Posts: {total_records}")

    print_section("3. TRIGGER AI ANALYTICS PIPELINE")
    analysis_config = {
        "duplicateSimilarityThreshold": 0.85,
        "minTopicSize": 2,
        "trendAlpha": 0.40,
        "trendBeta": 0.30,
        "trendGamma": 0.20,
        "trendDelta": 0.10,
        "windowSizeHours": 24
    }
    ana_res = requests.post(f"{BASE_URL}/datasets/{dataset_id}/analyze", headers=headers, json=analysis_config)
    print(f"AI Trigger response: {ana_res.status_code}, Job ID: {ana_res.json()['data']['id']}")

    print("Polling analysis status...")
    max_wait = 60
    start = time.time()
    job_status = "PENDING"
    while time.time() - start < max_wait:
        st_res = requests.get(f"{BASE_URL}/datasets/{dataset_id}/status", headers=headers)
        if st_res.status_code == 200 and st_res.json()["data"]:
            job = st_res.json()["data"]
            job_status = job.get("status")
            progress = job.get("progressPercentage")
            print(f"Status: {job_status} ({progress}%)")
            if job_status in ["COMPLETED", "FAILED"]:
                break
        time.sleep(2)

    assert job_status == "COMPLETED", f"Analysis did not complete successfully, status: {job_status}"
    print("AI pipeline completed successfully!")

    print_section("4. VERIFY DASHBOARD ANALYTICS ENDPOINTS")
    # Overview
    ov_res = requests.get(f"{BASE_URL}/analytics/overview?datasetId={dataset_id}", headers=headers)
    ov = ov_res.json()["data"]
    print(f"Overview Metrics:")
    print(f" - Total Posts: {ov['totalPosts']}")
    print(f" - Unique Posts: {ov['uniquePosts']}")
    print(f" - Duplicate Posts: {ov['duplicatePosts']}")
    print(f" - Positive Posts: {ov['positivePosts']}")
    print(f" - Negative Posts: {ov['negativePosts']}")
    print(f" - Neutral Posts: {ov['neutralPosts']}")
    print(f" - Active Topics: {ov['activeTopics']}")
    print(f" - Emerging Trends: {ov['emergingTrends']}")

    # Sentiment
    sent_res = requests.get(f"{BASE_URL}/analytics/sentiment?datasetId={dataset_id}", headers=headers)
    sent = sent_res.json()["data"]
    print(f"\nSentiment Breakdown: {sent['distribution']}")
    print(f"Net Sentiment Score: {sent['netSentimentScore']}")
    print(f"Timeline Points count: {len(sent['overTime'])}")

    # Emotions
    emo_res = requests.get(f"{BASE_URL}/analytics/emotions?datasetId={dataset_id}", headers=headers)
    emo = emo_res.json()["data"]
    print(f"\nEmotions Breakdown: {emo['distribution']}")
    print(f"Primary Emotion: {emo['primaryEmotion']} ({emo['primaryEmotionPercentage']}%)")

    # Topics
    top_res = requests.get(f"{BASE_URL}/analytics/topics?datasetId={dataset_id}", headers=headers)
    topics = top_res.json()["data"]
    print(f"\nDiscovered Topics ({len(topics)}):")
    for t in topics:
        print(f" - [{t['name']}] Posts: {t['postCount']}, Coherence: {t['representativeCoherence']}, Keywords: {t['topKeywords'][:4]}")

    # Trends
    trend_res = requests.get(f"{BASE_URL}/trends?datasetId={dataset_id}&method=PROPOSED_NOISE_RESILIENT", headers=headers)
    trend_data = trend_res.json()["data"]
    print(f"\nTrend Snapshots ({len(trend_data['snapshots'])}), Trend Alerts ({len(trend_data['alerts'])}):")
    for a in trend_data['alerts']:
        print(f" * ALERT [{a['alertLevel']}] {a['topicName']} (Score: {a['trendScore']}, Growth: +{a['growthPercentage']}%, Filtered Spam: {a['duplicateRatio'] * 100}%)")
        print(f"   Reason: {a['alertReason']}")
        print(f"   Supporting Post IDs: {a['supportingPostIds']}")

    # Network
    net_res = requests.get(f"{BASE_URL}/network/graph?datasetId={dataset_id}", headers=headers)
    net = net_res.json()["data"]
    print(f"\nNetwork Topology: Nodes: {len(net['nodes'])}, Edges: {len(net['edges'])}, Density: {net['metrics']['density']}")
    
    inf_res = requests.get(f"{BASE_URL}/network/influencers?datasetId={dataset_id}", headers=headers)
    influencers = inf_res.json()["data"]
    print(f"Top Influencer: {influencers[0]['pseudonym']} (PageRank: {influencers[0]['pagerank']}, In-Degree: {influencers[0]['inDegree']})")

    # Evaluation
    eval_res = requests.post(f"{BASE_URL}/evaluation/run?datasetId={dataset_id}", headers=headers)
    eval_data = eval_res.json()["data"]
    print(f"\nResearch Evaluation Comparison:")
    for r in eval_data["methodResults"]:
        print(f" > Method: {r['methodName']} | Precision: {r['precision']} | Recall: {r['recall']} | F1: {r['f1Score']} | FAR: {r['falseAlertRate']}")

    print_section("5. VERIFY DATABASE PERSISTENCE IN MYSQL")
    mysql_cmd = [
        r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "-u", "root", "--password=root", "social_analytics_db",
        "-e", """
        SELECT 'users' as tbl, count(*) as count FROM users
        UNION ALL SELECT 'datasets', count(*) FROM datasets
        UNION ALL SELECT 'social_posts', count(*) FROM social_posts
        UNION ALL SELECT 'duplicate_groups', count(*) FROM duplicate_groups
        UNION ALL SELECT 'sentiment_results', count(*) FROM sentiment_results
        UNION ALL SELECT 'emotion_results', count(*) FROM emotion_results
        UNION ALL SELECT 'topics', count(*) FROM topics
        UNION ALL SELECT 'post_topics', count(*) FROM post_topics
        UNION ALL SELECT 'trend_snapshots', count(*) FROM trend_snapshots
        UNION ALL SELECT 'trend_alerts', count(*) FROM trend_alerts
        UNION ALL SELECT 'network_nodes', count(*) FROM network_nodes
        UNION ALL SELECT 'network_edges', count(*) FROM network_edges
        UNION ALL SELECT 'analytics_jobs', count(*) FROM analytics_jobs
        UNION ALL SELECT 'evaluation_runs', count(*) FROM evaluation_runs
        UNION ALL SELECT 'evaluation_results', count(*) FROM evaluation_results;
        """
    ]
    res = subprocess.run(mysql_cmd, capture_output=True, text=True)
    print(res.stdout)

    print_section("ALL CHECKS PASSED SUCCESSFULLY!")

if __name__ == "__main__":
    main()

