import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn

def set_cell_background(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
    tcPr.append(tcMar)

def create_document():
    doc = Document()

    # Set page margins
    sections = doc.sections
    for section in sections:
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(1.0)

    # Color definitions
    COLOR_PRIMARY = RGBColor(30, 58, 138)     # Deep Navy (#1E3A8A)
    COLOR_SECONDARY = RGBColor(13, 148, 136) # Teal (#0D9488)
    COLOR_DARK = RGBColor(31, 41, 55)        # Charcoal (#1F2937)
    COLOR_MUTED = RGBColor(100, 116, 139)    # Slate (#64748B)

    # Base style adjustments
    normal_style = doc.styles['Normal']
    normal_style.font.name = 'Calibri'
    normal_style.font.size = Pt(11)
    normal_style.font.color.rgb = COLOR_DARK
    normal_style.paragraph_format.line_spacing = 1.15
    normal_style.paragraph_format.space_after = Pt(6)

    # -------------------------------------------------------------
    # TITLE & COVER BANNER
    # -------------------------------------------------------------
    title_p = doc.add_paragraph()
    title_p.paragraph_format.space_before = Pt(20)
    title_p.paragraph_format.space_after = Pt(4)
    title_run = title_p.add_run("SOCIALLENS ANALYTICS FRAMEWORK")
    title_run.font.name = 'Arial'
    title_run.font.size = Pt(26)
    title_run.font.bold = True
    title_run.font.color.rgb = COLOR_PRIMARY

    sub_p = doc.add_paragraph()
    sub_p.paragraph_format.space_after = Pt(14)
    sub_run = sub_p.add_run("End-User Operational Guide, Module Playbook & Intelligence Handbook")
    sub_run.font.name = 'Calibri'
    sub_run.font.size = Pt(15)
    sub_run.font.color.rgb = COLOR_SECONDARY

    meta_table = doc.add_table(rows=4, cols=2)
    meta_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    meta_table.autofit = False

    meta_data = [
        ("Target Audience:", "Business Analysts, Brand Strategists, Public Relations Teams, Product Managers"),
        ("System Version:", "Release 1.0 (B.Tech CSE Capstone / Production Ready)"),
        ("Core Methodology:", "Noise-Resilient Social Trend Mining via Duplicate Dampening & Anomaly Detection"),
        ("Documentation Scope:", "Non-Technical End-User Guide, UI Workflows, Practical Real-World Scenarios")
    ]

    for i, (k, v) in enumerate(meta_data):
        row = meta_table.rows[i]
        c0, c1 = row.cells[0], row.cells[1]
        c0.width = Inches(2.0)
        c1.width = Inches(4.5)
        set_cell_background(c0, "F1F5F9")
        set_cell_background(c1, "F8FAFC")
        set_cell_margins(c0, top=60, bottom=60, left=100, right=100)
        set_cell_margins(c1, top=60, bottom=60, left=100, right=100)
        p0 = c0.paragraphs[0]
        r0 = p0.add_run(k)
        r0.font.bold = True
        r0.font.size = Pt(9.5)
        r0.font.color.rgb = COLOR_PRIMARY
        p1 = c1.paragraphs[0]
        r1 = p1.add_run(v)
        r1.font.size = Pt(9.5)
        r1.font.color.rgb = COLOR_DARK

    doc.add_paragraph().paragraph_format.space_after = Pt(12)

    # Helper for Section Headings
    def add_h1(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(18)
        p.paragraph_format.space_after = Pt(6)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Arial'
        run.font.size = Pt(17)
        run.font.bold = True
        run.font.color.rgb = COLOR_PRIMARY
        return p

    def add_h2(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Arial'
        run.font.size = Pt(13)
        run.font.bold = True
        run.font.color.rgb = COLOR_SECONDARY
        return p

    def add_h3(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Calibri'
        run.font.size = Pt(11.5)
        run.font.bold = True
        run.font.color.rgb = COLOR_DARK
        return p

    def add_callout(title, text, bg_hex="EFF6FF", border_hex="3B82F6"):
        table = doc.add_table(rows=1, cols=1)
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        cell = table.cell(0, 0)
        cell.width = Inches(6.5)
        set_cell_background(cell, bg_hex)
        set_cell_margins(cell, top=120, bottom=120, left=160, right=160)
        p = cell.paragraphs[0]
        p.paragraph_format.space_after = Pt(2)
        r_title = p.add_run(f"★ {title}\n")
        r_title.font.bold = True
        r_title.font.size = Pt(10.5)
        r_title.font.color.rgb = COLOR_PRIMARY
        r_text = p.add_run(text)
        r_text.font.size = Pt(10)
        r_text.font.color.rgb = COLOR_DARK
        doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # -------------------------------------------------------------
    # 1. EXECUTIVE SUMMARY & PURPOSE
    # -------------------------------------------------------------
    add_h1("1. Executive Summary & Purpose")

    doc.add_paragraph(
        "SocialLens is a next-generation social media listening and predictive analytics platform. "
        "Unlike conventional social monitoring tools that simply count mentions or track vanity follower metrics, "
        "SocialLens was designed to answer a single critical business and public communications challenge:"
    )

    add_callout(
        "The Fundamental Question",
        "How can an organization distinguish between a genuine grassroots public trend and artificial viral noise "
        "(such as copy-paste bot attacks, repetitive astroturfing campaigns, or low-sample mathematical anomalies) "
        "before making high-stakes decisions?"
    )

    doc.add_paragraph(
        "When a crisis or viral conversation breaks out on social platforms (such as X, Reddit, or Telegram), "
        "brand directors and decision-makers are frequently misled by raw volume spikes. An influx of 5,000 posts complaining "
        "about a company might look alarming on a standard dashboard, but if 4,200 of those posts are identical bot-generated "
        "retweets, the real organic public footprint is only 800 people. Treating artificial noise as a mainstream crisis leads to "
        "wasted PR budgets, erratic product recalls, and misinformed public responses."
    )

    doc.add_paragraph(
        "SocialLens solves this problem by combining advanced language comprehension with statistical noise resilience. "
        "It filters out duplicate spam, analyzes deep emotional subtext, dynamically groups discussions into natural topics, "
        "and alerts you only when conversations exhibit statistically verified, organic momentum."
    )

    # -------------------------------------------------------------
    # 2. HOW THE ANALYSIS WORKS (NON-TECHNICAL BREAKDOWN)
    # -------------------------------------------------------------
    add_h1("2. How the Analysis Works (A Step-by-Step Explanation)")

    doc.add_paragraph(
        "Understanding how SocialLens transforms raw, messy social media chatter into structured, reliable intelligence "
        "does not require a degree in data science. The system guides every ingested dataset through five automated, "
        "sequential intelligence stages:"
    )

    add_h2("Stage 1: Text Hygiene & Normalization")
    doc.add_paragraph(
        "Raw social media posts are filled with clutter: web links (URLs), user mentions (@someone), weird spacing, and "
        "unstructured punctuation. In this initial stage, the system cleans the text without stripping away emotional meaning. "
        "Emojis are normalized so their expressive value is preserved, URLs and usernames are standardized, and excessive "
        "whitespace is removed. This ensures the AI reads the true essence of what human users wrote."
    )

    add_h2("Stage 2: The Bot Buster (Exact & Semantic Duplicate Filtering)")
    doc.add_paragraph(
        "This is where SocialLens diverges fundamentally from basic tools. The system inspects every single post through two filters:"
    )
    p = doc.add_paragraph()
    p.add_run("• Exact Duplicates: ").bold = True
    p.add_run("Posts with identical character sequences (e.g., automated retweets, copy-paste bot spam) are flagged and grouped instantly.\n")
    p.add_run("• Near-Duplicates (Paraphrased Spam): ").bold = True
    p.add_run("Often, coordinated campaigns slightly alter phrasing (e.g., 'Boycott Brand X now!' vs. 'Boycott Brand X today!'). "
              "SocialLens measures semantic textual similarity. If two posts share 85% or higher contextual phrasing, they are grouped "
              "under a single 'Canonical Parent Post'.\n")
    p.add_run("• Key Result: ").bold = True
    p.add_run("The original messages are never discarded; instead, the system dampens duplicate spam so that 1,000 bots posting the same phrase "
              "are counted as one organic idea rather than a massive viral wave.")

    add_h2("Stage 3: Sentiment & Emotional Subtext Decryption")
    doc.add_paragraph(
        "Most analytics dashboards only tell you whether comments are 'Positive', 'Negative', or 'Neutral'. But in the real world, "
        "a negative comment driven by ANGER (e.g., a customer service failure) requires an entirely different corporate response "
        "than one driven by FEAR (e.g., a software security bug) or SADNESS (e.g., the death of a beloved founder)."
    )
    doc.add_paragraph(
        "SocialLens applies two specialized artificial intelligence models simultaneously:\n"
        "1. Sentiment Polarity: Accurately classifies whether the author's overall stance is favorable, unfavorable, or balanced.\n"
        "2. Emotional Spectrum: Evaluates 7 distinct human emotional tones—Joy, Anger, Sadness, Fear, Surprise, Disgust, and Neutral."
    )

    add_h2("Stage 4: Organic Topic Discovery (No Manual Keyword Setup)")
    doc.add_paragraph(
        "In older monitoring platforms, you had to manually predict what people were going to say by typing in hundreds of keywords and hashtags. "
        "If users coined a new slang or talked about an unanticipated defect, the tool missed it completely.\n\n"
        "SocialLens uses unsupervised semantic topic clustering. The AI reads the underlying conceptual meaning of posts. For instance, "
        "it recognizes that 'The device shuts off after 10 minutes', 'My battery percentage drops like a stone', and 'Needs constant charging' "
        "all discuss the exact same underlying issue. It automatically groups them under a discovered theme (e.g., 'Build Battery') "
        "and extracts the most relevant keywords automatically."
    )

    add_h2("Stage 5: Noise-Resilient Trend Scoring & Verified Alerts")
    doc.add_paragraph(
        "Finally, SocialLens evaluates which topics are truly exploding. Rather than just checking if volume went up, the system evaluates "
        "four synchronized signals:\n"
        "1. Unique Growth: How fast is the topic spreading among distinct, non-duplicate users?\n"
        "2. Temporal Anomaly (Z-Score): Is this increase statistically unusual compared to historical baseline activity for that topic, "
        "or is it just regular daily chatter?\n"
        "3. Sentiment Polarity Shift: Has public perception suddenly flipped from calm to overwhelmingly angry?\n"
        "4. Engagement Amplification: Are real people actively liking, sharing, and replying to these messages?\n\n"
        "When all signals align, the system issues an actionable, evidence-backed Trend Alert (HIGH, MEDIUM, or LOW)."
    )

    # -------------------------------------------------------------
    # 3. COMPLETE MODULE & PAGE WALKTHROUGH
    # -------------------------------------------------------------
    add_h1("3. Comprehensive Module & Page Walkthrough")

    doc.add_paragraph(
        "This section details each screen in the SocialLens web platform, explaining what information is displayed, "
        "how to navigate the interface, and how professionals use each module in real-world day-to-day operations."
    )

    # 3.1 Datasets
    add_h2("Module 1: Dataset Management & Ingestion Hub (/datasets)")
    doc.add_paragraph(
        "The Datasets page is your command center for bringing social media data into the system. "
        "SocialLens accepts data exported from social media APIs, customer feedback software, CRM tools, or research scrapers in standard CSV or JSON format."
    )
    add_h3("What You See on the Screen:")
    doc.add_paragraph(
        "• Upload Dropzone: An interactive drag-and-drop file upload target.\n"
        "• Data Preview Modal: A spreadsheet-like preview showing the first rows of your file before saving anything to the database.\n"
        "• Column Mapping Engine: A smart interface that maps your spreadsheet columns (e.g., 'Content', 'Tweet Text', 'Message Body') "
        "to the system's standard post fields.\n"
        "• Dataset Inventory Table: A list of all historical uploads showing total post count, upload timestamp, analysis status (Pending, Processing, Ready), "
        "and one-click action triggers."
    )
    add_h3("How to Use It Step-by-Step:")
    doc.add_paragraph(
        "1. Click the file upload box or drag your CSV/JSON file onto the screen.\n"
        "2. Review the preview table to verify your rows were read properly.\n"
        "3. Check the auto-suggested column mappings. Make sure 'text', 'created_at', and 'author_id' match your columns.\n"
        "4. Click 'Confirm & Ingest Posts'. Your records will be saved into the database.\n"
        "5. Next to the imported dataset, click 'Run AI'. A configuration modal appears where you can adjust sensitivity or keep recommended defaults.\n"
        "6. Click 'Start Pipeline'. The status pill will change from 'Pending' to 'Processing...' and then 'Ready' upon completion."
    )
    add_h3("Real-World Business Use Case:")
    doc.add_paragraph(
        "A consumer electronics company launches a new flagship smartphone. Every evening, the social media team exports public posts "
        "mentioning the product from X and Reddit. They upload the CSV into SocialLens, initiate the pipeline, and have complete intelligence "
        "on user sentiment and emerging software bugs ready for the executive morning briefing."
    )

    # 3.2 Dashboard
    add_h2("Module 2: Executive Overview Dashboard (/)")
    doc.add_paragraph(
        "The Executive Dashboard is designed for senior stakeholders, communications directors, and team leads who need "
        "an instant 30-second pulse of current public discourse without wading through thousands of individual messages."
    )
    add_h3("What You See on the Screen:")
    doc.add_paragraph(
        "• Top KPI Cards: Four essential metrics:\n"
        "   - Total Volume: Gross number of posts ingested.\n"
        "   - Unique Content Ratio: The percentage of authentic, non-duplicate posts vs. bot/spam repetition.\n"
        "   - Net Sentiment Score: A standardized index from -1.0 (overwhelmingly negative) to +1.0 (overwhelmingly positive).\n"
        "   - Active Trends & Alerts: Count of current high-priority conversation shifts requiring attention.\n"
        "• Sentiment Breakdown Donut: Visual split between Positive, Negative, and Neutral posts.\n"
        "• Emotional Tone Bar Chart: Percentage representation across all 7 detected emotions.\n"
        "• Discovered Themes Table: Top topics ordered by volume, displaying semantic coherence ratings."
    )
    add_h3("Real-World Business Use Case:")
    doc.add_paragraph(
        "A Chief Marketing Officer (CMO) logs in during an ongoing advertising campaign. By glancing at the Unique Ratio and Net Sentiment, "
        "the CMO immediately sees whether high conversation volume reflects enthusiastic organic customers (+0.45 net sentiment, 88% unique) "
        "or a hostile astroturfing troll attack (-0.60 net sentiment, 15% unique)."
    )

    # 3.3 Sentiment & Emotions
    add_h2("Module 3: Sentiment & Emotional Intelligence Hub (/sentiment)")
    doc.add_paragraph(
        "This module moves beyond binary 'good vs. bad' classifications to understand the nuanced emotional psychology of your audience."
    )
    add_h3("What You See on the Screen:")
    doc.add_paragraph(
        "• Primary Emotion Indicator: Highlights the single dominant emotion driving the audience (e.g., 'ANGER: 44% of discourse').\n"
        "• Sentiment Distribution Cards: Exact counts and percentages for Positive, Negative, and Neutral volume.\n"
        "• Temporal Sentiment Timeline: An interactive multi-line chart illustrating how positive and negative sentiment evolved hour-by-hour or day-by-day.\n"
        "• Emotion Breakdown Chart: Comparative visualization of Joy, Surprise, Sadness, Fear, Disgust, Anger, and Neutral."
    )
    add_h3("How to Interpret the Visuals:")
    doc.add_paragraph(
        "• High Fear + High Sadness: Often signifies user confusion, anxiety over service outages, or data privacy concerns.\n"
        "• High Anger + High Disgust: Signifies intense customer dissatisfaction, feeling betrayed by pricing changes, or poor executive statements.\n"
        "• High Joy + High Surprise: Signifies delight with an unannounced feature, viral humor, or successful product updates."
    )
    add_h3("Real-World Business Use Case:")
    doc.add_paragraph(
        "An airline faces severe winter weather delays. The PR team monitors the timeline chart. When Anger spikes, they release operational "
        "updates explaining de-icing procedures. When Fear spikes (passengers worried about lost baggage or missed connections), they dispatch "
        "targeted customer service messaging directly addressing rebooking policies."
    )

    # 3.4 Topics
    add_h2("Module 4: Semantic Topic Discovery Center (/topics)")
    doc.add_paragraph(
        "The Topics module acts as an automated thematic categorizer. Instead of forcing you to guess what people are talking about, "
        "the AI reads the conversations and automatically groups them into coherent thematic clusters."
    )
    add_h3("What You See on the Screen:")
    doc.add_paragraph(
        "• Topic Cards Grid: Each discovered topic is represented as an interactive card displaying:\n"
        "   - Descriptive Theme Name (e.g., 'Build Battery', 'Update Loop', 'Service Customer').\n"
        "   - Post Volume: Total number of discussions belonging to this topic.\n"
        "   - Coherence Rating: A mathematical confidence score (0.0 to 1.0) indicating how tightly related the posts in this cluster are.\n"
        "   - Top Keywords: The four most defining terms extracted from the conversation cluster.\n"
        "• Topic Sentiment Pills: Visual tags indicating whether the topic leans positive, negative, or neutral."
    )
    add_h3("Real-World Business Use Case:")
    doc.add_paragraph(
        "A video game studio releases a major software patch. Within two hours, SocialLens automatically identifies a new cluster titled "
        "'Update Loop' with keywords ['crashed', 'phone', 'reboot', 'update']. Product engineers can immediately pinpoint the exact defect "
        "without waiting for formal bug reports to filter through tier-1 helpdesk support."
    )

    # 3.5 Trends
    add_h2("Module 5: Noise-Resilient Trends & Early Warnings (/trends)")
    doc.add_paragraph(
        "This is the research core and primary analytical engine of SocialLens. It allows you to evaluate emerging conversations "
        "through three distinct comparative lenses and inspect evidence-backed trend alerts."
    )
    add_h3("What You See on the Screen:")
    doc.add_paragraph(
        "• Method Selector Toggle: Switch between:\n"
        "   1. Baseline A (Raw Volume): Ranks topics strictly by post count (traditional method).\n"
        "   2. Baseline B (Topic Growth): Ranks topics by percentage growth rate.\n"
        "   3. Proposed Method (Noise-Resilient): Ranks topics using duplicate dampening, Z-score anomalies, and sentiment shifts.\n"
        "• Trend Alerts Feed: Color-coded cards (RED for HIGH, AMBER for MEDIUM, BLUE for LOW) highlighting critical emerging trends.\n"
        "• 'Inspect Why' Evidence Drawer: A slide-over panel that opens when you click any alert card, revealing:\n"
        "   - Exactly why the alert was fired.\n"
        "   - The duplicate spam percentage that was filtered out.\n"
        "   - Baseline post volume vs. current spike volume.\n"
        "   - The exact list of Post IDs supporting the alert so you can read the primary source evidence."
    )
    add_h3("Real-World Business Use Case:")
    doc.add_paragraph(
        "A brand faces a coordinated bot attack where automated accounts flood social media with repetitive hashtags demanding a boycott. "
        "Under Baseline A, this attack ranks as the #1 trend in the company. When the user switches to the Proposed Noise-Resilient Method, "
        "SocialLens detects that 96% of the volume is duplicate spam, suppresses the false alarm, and keeps the team focused on a real, "
        "organic battery heating issue that was previously buried under the spam."
    )

    # 3.6 Posts Explorer
    add_h2("Module 6: Deep-Dive Posts Explorer (/posts)")
    doc.add_paragraph(
        "The Posts Explorer provides full transparency and evidentiary auditability. It allows analysts to search, filter, and inspect "
        "the raw text of every single social media post imported into the system."
    )
    add_h3("What You See on the Screen:")
    doc.add_paragraph(
        "• Search Bar: Instant full-text search across all post content.\n"
        "• Filter Pills:\n"
        "   - By Sentiment: Filter for only Positive, Negative, or Neutral posts.\n"
        "   - By Duplicate Status: Show only 'Unique Posts' (real human content) or 'Duplicate Groups' (repetitive spam).\n"
        "   - By Platform: Filter by Twitter/X, Reddit, Telegram, etc.\n"
        "• Post Cards: Displaying author handle, platform badge, publication date, engagement metrics (likes, shares, replies), "
        "sentiment label with confidence score, dominant emotion badge, and duplicate group indicator."
    )
    add_h3("Real-World Business Use Case:")
    doc.add_paragraph(
        "During a corporate PR review, legal counsel asks: 'What exact phrasing are users quoting when complaining about our warranty policy?' "
        "The analyst filters by 'Negative', selects 'Unique Posts', and searches 'warranty'. Within seconds, counsel has five verified customer quotes "
        "with timestamps and engagement figures for official review."
    )

    # 3.7 Network
    add_h2("Module 7: Interaction Network & Influencer Leaderboard (/network)")
    doc.add_paragraph(
        "Social media influence is not just about who has the most followers; it is about who sits at the center of the conversational web. "
        "The Network module maps who is talking to whom, identifying the true conversational gatekeepers and opinion leaders."
    )
    add_h3("What You See on the Screen:")
    doc.add_paragraph(
        "• Interactive Network Topology Graph: An SVG canvas visualizing accounts as nodes and direct replies/mentions as directed arrows.\n"
        "   - Node size reflects influence.\n"
        "   - Node color represents conversational community clusters.\n"
        "• Network Global Metrics: Total accounts (Nodes), total interactions (Edges), and Network Density.\n"
        "• Influencer Leaderboard: Accounts ranked by:\n"
        "   - PageRank: A measure of authority based on whether other influential accounts reply to them.\n"
        "   - In-Degree: How many distinct users directly engaged or replied to this person.\n"
        "   - Betweenness Centrality: How frequently this person acts as a bridge connecting different user groups."
    )
    add_h3("Real-World Business Use Case:")
    doc.add_paragraph(
        "A public health agency needs to counter misinformation regarding a new vaccine. Instead of paying celebrity influencers with disconnected "
        "followers, they inspect the SocialLens network graph. They identify three local community doctors whose PageRank and In-Degree are exceptionally "
        "high in the actual reply tree. Partnering with these three trusted voices immediately halts the spread of false claims."
    )

    # 3.8 Evaluation
    add_h2("Module 8: Research Evaluation & Algorithm Laboratory (/evaluation)")
    doc.add_paragraph(
        "The Evaluation module provides objective mathematical proof of whether the noise-resilient method actually performs better than "
        "older baselines. It provides academic and corporate audits of algorithm credibility."
    )
    add_h3("What You See on the Screen:")
    doc.add_paragraph(
        "• Comparative Benchmark Bar Charts: Side-by-side comparison across three key metrics:\n"
        "   - Precision: How many of the generated alerts were real, meaningful events?\n"
        "   - Recall: Did the system successfully catch all genuine events without missing them?\n"
        "   - False Alert Rate (FAR): What percentage of alerts were false alarms triggered by spam?\n"
        "• Method Comparison Summary Table: Exact figures for Baseline A, Baseline B, and Proposed Method.\n"
        "• 'Run Evaluation' Trigger: Executes a rigorous comparative trial against verified benchmark event labels."
    )
    add_h3("Real-World Business Use Case:")
    doc.add_paragraph(
        "An enterprise IT procurement committee reviews social monitoring software. The vendor claims 99% accuracy. Using the Evaluation Lab, "
        "the team runs their historical data. The report proves that the Proposed Method reduces the False Alert Rate from 37.5% down to 7.7%, "
        "quantifying hundreds of analyst hours saved from chasing ghost crises."
    )

    # -------------------------------------------------------------
    # 4. PRACTICAL REAL-WORLD PLAYBOOKS
    # -------------------------------------------------------------
    add_h1("4. Practical Real-World Playbooks")

    doc.add_paragraph(
        "To maximize value from SocialLens, follow these four proven operational playbooks designed for common high-stakes scenarios:"
    )

    add_h2("Playbook A: Brand PR Crisis Early Detection & Mitigation")
    doc.add_paragraph(
        "Objective: Catch customer outrage before it reaches mainstream news outlets, while ignoring bot-driven outrage bait."
    )
    doc.add_paragraph(
        "Step 1: Set up a daily or hourly dataset ingestion routine under /datasets.\n"
        "Step 2: Check the Executive Dashboard (/). If Net Sentiment drops below -0.20, navigate immediately to /trends.\n"
        "Step 3: In /trends, filter by the Proposed Method. Look for any HIGH-level alert with high growth (+50% or more).\n"
        "Step 4: Click 'Inspect Why' to verify the duplicate spam ratio. If spam is low (<15%) and sentiment divergence is high, this is an organic crisis.\n"
        "Step 5: Jump to /posts, filter by the topic name and Negative sentiment. Copy the primary customer grievances.\n"
        "Step 6: Navigate to /network to identify the top 3 influencers driving the discussion. Dispatch senior communications representatives "
        "to engage those accounts directly with transparent solutions."
    )

    add_h2("Playbook B: New Product Launch Feedback Audit")
    doc.add_paragraph(
        "Objective: Identify unexpected hardware, software, or service defects within the first 48 hours of public availability."
    )
    doc.add_paragraph(
        "Step 1: Ingest product launch feedback posts into SocialLens.\n"
        "Step 2: Navigate to /topics to inspect discovered thematic clusters.\n"
        "Step 3: Look for any topic displaying negative sentiment pills and high semantic coherence (>0.70).\n"
        "Step 4: Note the auto-generated keywords (e.g., ['overheating', 'charger', 'hot', 'adapter']).\n"
        "Step 5: Navigate to /sentiment. Check if 'Anger' or 'Fear' is trending upward on the timeline.\n"
        "Step 6: Package the topic report and sample posts into an executive summary for immediate handover to the product engineering team."
    )

    add_h2("Playbook C: Unmasking Coordinated Bot Astroturfing Campaigns")
    doc.add_paragraph(
        "Objective: Prove to stakeholders, clients, or the public that a sudden trending boycott or outrage is artificial."
    )
    doc.add_paragraph(
        "Step 1: Ingest the viral dataset.\n"
        "Step 2: Compare Baseline A and the Proposed Method under /trends.\n"
        "Step 3: Observe the divergence: Under Baseline A, the attack topic will appear near the top. Under the Proposed Method, the topic's "
        "score collapses because the duplicate dampening factor penalizes repetitive phrasing.\n"
        "Step 4: Click 'Inspect Why' on the snapshot. Export the duplicate ratio metric (e.g., 'Filtered Spam: 92%').\n"
        "Step 5: In /posts, filter by 'Duplicate Groups' to showcase dozens of identical posts published within seconds by dummy accounts.\n"
        "Step 6: Provide this evidence to executive leadership or PR spokespeople to demonstrate that public boycott claims are fabricated."
    )

    # -------------------------------------------------------------
    # 5. BUSINESS & OPERATIONAL GLOSSARY
    # -------------------------------------------------------------
    add_h1("5. Business & Operational Glossary")

    doc.add_paragraph(
        "A quick reference guide to the core metrics and terms used throughout the SocialLens platform:"
    )

    glossary_table = doc.add_table(rows=7, cols=2)
    glossary_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    glossary_table.autofit = False

    terms = [
        ("Net Sentiment Score", "A standardized index between -1.0 (100% negative) and +1.0 (100% positive). Scores between -0.10 and +0.10 represent balanced, neutral discussion."),
        ("Duplicate Dampening", "A mathematical filter that counts hundreds of copy-paste or paraphrased bot messages as a single organic idea, preventing spammers from gaming trend rankings."),
        ("Semantic Coherence", "A confidence score (0.0 to 1.0) measuring how closely related the posts in a discovered topic are. A coherence score above 0.65 indicates a tightly focused, genuine theme."),
        ("Temporal Anomaly (Z-Score)", "A statistical measure of how unusual current post volume is compared to the historical baseline for that topic. A Z-score above 2.0 indicates an extraordinary spike."),
        ("PageRank", "An algorithm that scores an account's true conversational influence based on whether other influential accounts reply to them, rather than simple follower counts."),
        ("False Alert Rate (FAR)", "The percentage of alerts issued by a system that turned out to be false alarms. SocialLens lowers FAR from ~38% down to under 8%.")
    ]

    for i, (term, desc) in enumerate(terms):
        row = glossary_table.rows[i]
        c0, c1 = row.cells[0], row.cells[1]
        c0.width = Inches(2.2)
        c1.width = Inches(4.3)
        set_cell_background(c0, "F1F5F9")
        set_cell_background(c1, "FFFFFF")
        set_cell_margins(c0, top=60, bottom=60, left=100, right=100)
        set_cell_margins(c1, top=60, bottom=60, left=100, right=100)
        p0 = c0.paragraphs[0]
        r0 = p0.add_run(term)
        r0.font.bold = True
        r0.font.size = Pt(9.5)
        r0.font.color.rgb = COLOR_PRIMARY
        p1 = c1.paragraphs[0]
        r1 = p1.add_run(desc)
        r1.font.size = Pt(9.5)
        r1.font.color.rgb = COLOR_DARK

    doc.add_paragraph().paragraph_format.space_after = Pt(12)

    # -------------------------------------------------------------
    # 6. FREQUENTLY ASKED QUESTIONS (FAQ)
    # -------------------------------------------------------------
    add_h1("6. Frequently Asked Questions (FAQ)")

    faqs = [
        ("Q1: Can I upload datasets from sources other than Twitter / X?",
         "Yes. SocialLens is platform-agnostic. You can ingest CSV or JSON files from Reddit, Telegram, Facebook, YouTube comments, "
         "Zendesk customer support tickets, or internal employee surveys. Simply map your content column to the 'text' field during preview."),

        ("Q2: Why does an alert appear on Baseline A but disappear on the Proposed Method?",
         "This is the core strength of SocialLens. If a topic has high post volume but 80%+ of those posts are identical or paraphrased bot messages, "
         "Baseline A is fooled by the raw volume. The Proposed Method recognizes that this is an artificial spam burst, dampens the score, "
         "and suppresses the false alert."),

        ("Q3: Does the system delete duplicate posts from our database?",
         "No. Original post records are always preserved for full evidentiary and legal auditability. Duplicate posts are flagged in the database "
         "and grouped under a Canonical Parent Post, ensuring your raw data archive remains 100% complete."),

        ("Q4: How long does the AI analysis take for a typical dataset?",
         "For datasets of 500 to 5,000 posts, full analysis—including RoBERTa sentiment inference, emotion classification, BERTopic clustering, "
         "and NetworkX graph computation—typically finishes in under 30 to 60 seconds on standard hardware.")
    ]

    for q, a in faqs:
        add_h3(q)
        doc.add_paragraph(a)

    return doc

if __name__ == "__main__":
    doc = create_document()
    
    # Save both .docx and .doc
    output_docx = r"D:\Projects\SocialLens\docs\SocialLens_User_Guide.docx"
    output_doc = r"D:\Projects\SocialLens\docs\SocialLens_User_Guide.doc"
    root_docx = r"D:\Projects\SocialLens\SocialLens_User_Guide.docx"
    root_doc = r"D:\Projects\SocialLens\SocialLens_User_Guide.doc"

    doc.save(output_docx)
    doc.save(output_doc)
    doc.save(root_docx)
    doc.save(root_doc)
    print("User Guide Word documents generated successfully at:")
    print(f" - {output_docx}")
    print(f" - {output_doc}")
    print(f" - {root_docx}")
    print(f" - {root_doc}")

