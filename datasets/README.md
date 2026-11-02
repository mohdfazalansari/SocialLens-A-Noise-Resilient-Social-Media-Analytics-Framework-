# SocialLens Sample Datasets

> **ACADEMIC NOTICE & ETHICAL DATA GUIDELINES**  
> All records in `sample_posts.csv` and `sample_posts.json` are **strictly synthetic** benchmark datasets generated specifically for testing, algorithm verification, and project demonstration.  
> They do not represent real social media users, actual private observations, or copyrighted social media platform data.

---

## Dataset Schema Specifications

The platform supports generic tabular (CSV) and structured (JSON) imports. Column headers are automatically recognized by the schema mapper:

| Canonical Field | Expected Type | Description |
| :--- | :--- | :--- |
| `post_id` | String / Integer | Unique identifier of the social media post |
| `platform` | String | Source platform (`TWITTER`, `REDDIT`, `TELEGRAM`, etc.) |
| `author_id` | String | Pseudonymized author account identifier |
| `text` | String (Required) | Textual payload of the post |
| `created_at` | Timestamp | ISO-8601 or standard datetime (`YYYY-MM-DD HH:MM:SS`) |
| `like_count` | Integer | Aggregate positive feedback / upvotes |
| `reply_count` | Integer | Observable comment or reply count |
| `share_count` | Integer | Reposts, retweets, or shares |
| `reply_to_post_id`| String | Target post ID for reply chain & interaction graph construction |

---

## Synthetically Injected Test Scenarios

The sample dataset (`sample_posts.csv`) contains 25 calibrated posts modeling four distinct social scenarios:

1. **Repetitive Bot Spam (Duplicate Dampening Verification)**
   - Posts `1001` - `1004` share identical phrasing regarding battery drain.
   - **Expected Result**: Exact hashing groups posts into a single cluster. The duplicate ratio increases to 75% for this group, preventing artificial inflation in trend scores.

2. **Organic High-Volume Sentiment (Camera Optics Praise)**
   - Posts `1010` - `1014` exhibit diverse vocabulary and high positive polarity.
   - **Expected Result**: BERTopic clusters these into a positive topic with high coherence and low duplicate ratio.

3. **Sudden Anomaly Surge (Crash & Boot Loop Bug)**
   - Posts `1015` - `1021` show a sudden burst in unique posts within a short time window on October 26th.
   - **Expected Result**: Triggers an `EMERGING` trend alert with high Z-score anomaly and marked sentiment divergence ($\Delta S$).

4. **Interactive Support Network (NetworkX Graph Verification)**
   - Posts `1008`, `1009`, `1018`, `1019`, `1024`, `1025` form a directed interaction graph with handle `support_team`.
   - **Expected Result**: PageRank identifies `support_team` as the primary interaction hub with the highest in-degree centrality.

