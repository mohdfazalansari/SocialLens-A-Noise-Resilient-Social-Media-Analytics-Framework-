# Research Methodology & Noise-Resilient Trend Detection

## 1. Problem Statement & Research Hypothesis

In social media analytics, conventional trend detection algorithms rely heavily on **raw post-volume frequency** or **percentage growth rates**. While straightforward, these methods are acutely vulnerable to artificial noise:
- Coordinated bot rings and repetitive duplicate retweets artificially inflate raw volume.
- Percentage growth metrics spike violently on low-volume statistical anomalies (e.g., 1 post to 4 posts = +300% growth).
- Volume alone fails to identify opinion divergence or turning points in public discourse.

### Core Research Question
> **Can combining duplicate-aware dampening, temporal anomaly detection (Z-scores), and sentiment-shift analysis improve emerging-trend detection accuracy (precision/recall/F1) and lower false-alert rates compared with simple volume-based baselines?**

---

## 2. Mathematical Formulations

### Baseline A: Raw Post Volume ($B_1$)
For a given topic $k$ within temporal window $W_t$:
$$\text{Score}_{B1}(k, W_t) = V(k, W_t)$$
Where $V(k, W_t)$ is the raw number of posts in window $W_t$.

### Baseline B: Topic Growth & Engagement ($B_2$)
$$\text{Score}_{B2}(k, W_t) = \frac{V(k, W_t) - V(k, W_{t-1})}{\max(1, V(k, W_{t-1}))} \times \log_2(1 + E(k, W_t))$$
Where $E(k, W_t) = \text{Likes} + \text{Replies} + \text{Shares}$.

### Proposed Method: Noise-Resilient Multi-Signal Trend Score ($M_{prop}$)

1. **Deduplicated Volume ($V_{uniq}$):**
   $$V_{uniq}(k, W_t) = \sum_{p \in \text{Posts}(k, W_t)} w_{uniq}(p)$$
   Where $w_{uniq}(p) = \frac{1}{\text{cluster\_size}(p)}$ if $p$ belongs to a duplicate group, else $1.0$.

2. **Temporal Anomaly Score ($Z_{score}$):**
   Using a rolling baseline of historical windows ($\mu_k, \sigma_k$):
   $$Z(k, W_t) = \frac{V_{uniq}(k, W_t) - \mu_k}{\max(1.0, \sigma_k)}$$

3. **Sentiment Volatility / Shift ($\Delta S$):**
   $$\Delta S(k, W_t) = |\text{Polarity}(k, W_t) - \text{Polarity}_{baseline}(k)|$$
   Where $\text{Polarity} = \frac{N_{pos} - N_{neg}}{N_{pos} + N_{neg} + 1e-5}$. Sudden sentiment swings often signal authentic emergent real-world inflection points.

4. **Composite Formulation:**
   $$\text{TrendScore}(k, W_t) = \alpha \cdot \text{Sigmoid}(Z(k, W_t)) + \beta \cdot \text{GrowthRate}_{uniq}(k, W_t) + \gamma \cdot \Delta S(k, W_t) + \delta \cdot \min(1.0, \log_{10}(1 + E))$$
   *Configurable Baseline Weights:* $\alpha = 0.40, \beta = 0.30, \gamma = 0.20, \delta = 0.10$.

---

## 3. Classification & Alerting Rules

- **EMERGING**: $Z \ge 1.8$ with $\text{Growth}_{uniq} \ge 50\%$ and duplicate spam ratio $< 0.50$.
- **RISING**: $Z \ge 0.8$ with $\text{Growth}_{uniq} \ge 25\%$.
- **STABLE**: $|\text{Growth}_{uniq}| < 25\%$ and $-1.0 < Z < 0.8$.
- **DECLINING**: $\text{Growth}_{uniq} \le -25\%$ or $Z \le -1.0$.

---

## 4. Evaluation Benchmark Metrics

Experiments compare all three approaches across standard information retrieval metrics against ground-truth validation labels:
- **Precision:** $\frac{TP}{TP + FP}$
- **Recall:** $\frac{TP}{TP + FN}$
- **F1-Score:** $2 \cdot \frac{\text{Precision} \cdot \text{Recall}}{\text{Precision} + \text{Recall}}$
- **False-Alert Rate:** $\frac{FP}{TP + FP}$
- **Average Detection Delay:** $\frac{1}{K} \sum_{k=1}^K (t_{alert, k} - t_{onset, k})$ in hours.

