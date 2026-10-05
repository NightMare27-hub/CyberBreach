import json
import random
import datetime

topics = [
    {"concept": "bruteforce", "track": "Access", "fix": "block_ip"},
    {"concept": "sql_injection", "track": "Web App", "fix": "patch"},
    {"concept": "xss", "track": "Web App", "fix": "patch"},
    {"concept": "ddos", "track": "Networking", "fix": "block_ip"},
    {"concept": "open_port", "track": "Networking", "fix": "close_port"},
    {"concept": "ransomware", "track": "Malware", "fix": "isolate_host"},
    {"concept": "phishing", "track": "Social Eng", "fix": "quarantine_email"},
    {"concept": "zero_day", "track": "Defense", "fix": "patch"},
    {"concept": "mitm", "track": "Networking", "fix": "enable_encryption"}
]

orgs = ["St. Mary's Hospital", "Greenfield High School", "BlueCart Online Store", "OmniCorp Global", "NeoTech Startups", "Global Bank Inc.", "Federal Archive", "City Power Grid"]

ips = ["192.168.1.", "10.0.0.", "172.16.0.", "203.0.113.", "198.51.100.", "200.10.15."]

levels = []

def random_ip():
    return random.choice(ips) + str(random.randint(1, 254))

def generate_logs(topic, malicious_ip, target_port):
    logs = []
    base_time = datetime.datetime.now().replace(hour=10, minute=0, second=0, microsecond=0)
    for i in range(20):
        t = (base_time + datetime.timedelta(seconds=i*3)).strftime("%H:%M:%S")
        is_malicious = random.random() < 0.2
        if is_malicious:
            src = malicious_ip
            port = target_port
            if topic["concept"] == "bruteforce":
                event, detail = "LOGIN_FAIL", "User admin, wrong password"
            elif topic["concept"] == "sql_injection":
                event, detail = "HTTP_500", "SQL Syntax error in query parameter"
            elif topic["concept"] == "xss":
                event, detail = "HTTP_REQ", "Payload contains <script> tag"
            elif topic["concept"] == "ddos":
                event, detail = "SYN_FLOOD", "Abnormal SYN requests detected"
            elif topic["concept"] == "open_port":
                event, detail = "TELNET_OPEN", "Unencrypted remote login"
            elif topic["concept"] == "ransomware":
                event, detail = "SMB_WRITE", "Mass file encryption detected"
            elif topic["concept"] == "phishing":
                event, detail = "SMTP_IN", "Suspicious sender domain mimicking IT support"
            elif topic["concept"] == "zero_day":
                event, detail = "EXPLOIT_ATTEMPT", "Unknown heap spray pattern"
            elif topic["concept"] == "mitm":
                event, detail = "ARP_REPLY", "Unexpected ARP broadcast for gateway"
            else:
                event, detail = "MALICIOUS", "Unknown threat"
        else:
            src = random_ip()
            port = random.choice([80, 443, 53, 22])
            if port == 443:
                event, detail = "HTTPS_OK", "Normal encrypted traffic"
            elif port == 80:
                event, detail = "HTTP_OK", "Normal web request"
            elif port == 53:
                event, detail = "DNS_QUERY", "Resolving domain name"
            elif port == 22:
                event, detail = "SSH_OK", "Authorized key login"
        logs.append({"time": t, "src": src, "port": port, "event": event, "detail": detail})
    
    # Ensure at least one malicious log
    if not any(l["src"] == malicious_ip for l in logs):
        logs.append({"time": (base_time + datetime.timedelta(seconds=60)).strftime("%H:%M:%S"), "src": malicious_ip, "port": target_port, "event": "ATTACK", "detail": "Guaranteed attack packet"})
    
    # Sort logs by time
    logs.sort(key=lambda x: x["time"])
    return logs

level_id_counter = 1
for topic in topics:
    for difficulty in range(1, 6): # 5 levels per topic
        target_port = 22 if topic["concept"] in ["bruteforce", "open_port"] else (80 if topic["concept"] in ["sql_injection", "xss"] else random.choice([443, 80, 21, 3389]))
        malicious_ip = random_ip()
        target = malicious_ip if topic["fix"] in ["block_ip", "isolate_host"] else (str(target_port) if topic["fix"] == "close_port" else ("EXPLOIT_ATTEMPT" if topic["fix"] == "patch" else "SMTP_IN"))
        
        level = {
            "id": f"{topic['concept']}_{difficulty:02d}",
            "title": f"{topic['concept'].replace('_', ' ').title()} - Level {difficulty}",
            "org": random.choice(orgs),
            "track": topic['track'],
            "concept": topic['concept'],
            "timeLimitSec": max(30, 150 - (difficulty * 20)),
            "vulnerability": {
                "type": topic["concept"],
                "fix": topic["fix"],
                "target": target
            },
            "debrief": f"Educational module on {topic['concept']}. Mitigation applied: {topic['fix']}.",
            "hints": [
                f"Look for anomalies related to {topic['concept']}.",
                f"Check traffic from {malicious_ip} or on port {target_port}.",
                f"Use the {topic['fix']} tool."
            ],
            "logs": generate_logs(topic, malicious_ip, target_port)
        }
        levels.append(level)
        level_id_counter += 1

output = {
    "version": 2,
    "pack": "Expanded SOC Operations",
    "levels": levels
}

with open("app/src/main/assets/levels.json", "w") as f:
    json.dump(output, f, indent=2)
print(f"Generated {len(levels)} levels.")

