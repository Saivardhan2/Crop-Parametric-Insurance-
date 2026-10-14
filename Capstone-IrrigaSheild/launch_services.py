import os
import subprocess
import time
import socket
import sys

sys.stdout.reconfigure(encoding='utf-8')

JAVA_EXE = r'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\java.exe'
BASE_DIR = r'D:\Capstone\Capstone-IrrigaSheild'
LOG_DIR = os.path.join(BASE_DIR, 'runtime-logs')
os.makedirs(LOG_DIR, exist_ok=True)

SERVICES = [
    ('eureka-server', 8761),
    ('auth-service', 8081),
    ('farm-service', 8082),
    ('policy-service', 8083),
    ('event-claims-service', 8084),
    ('api-gateway', 8080),
]

def is_port_open(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.settimeout(0.5)
        return s.connect_ex(('127.0.0.1', port)) == 0

procs = []

for name, port in SERVICES:
    if is_port_open(port):
        print(f'[{name}] already running on port {port}.', flush=True)
        continue
    jar_path = os.path.join(BASE_DIR, name, 'target', f'{name}-0.0.1-SNAPSHOT.jar')
    log_path = os.path.join(LOG_DIR, f'{name}.log')
    print(f'Starting [{name}] on port {port}...', flush=True)
    log_file = open(log_path, 'a', encoding='utf-8')
    cmd = [
        JAVA_EXE,
        '-Deureka.instance.ip-address=127.0.0.1',
        '-Deureka.instance.prefer-ip-address=true',
        '-jar',
        jar_path
    ]
    p = subprocess.Popen(
        cmd,
        cwd=os.path.join(BASE_DIR, name),
        stdout=log_file,
        stderr=subprocess.STDOUT
    )
    procs.append(p)
    print(f'[{name}] spawned with PID {p.pid}.', flush=True)
    if name == 'eureka-server':
        time.sleep(6)

print('Waiting for all microservice ports to become available...', flush=True)
start_time = time.time()
while time.time() - start_time < 90:
    all_open = True
    for name, port in SERVICES:
        if not is_port_open(port):
            all_open = False
            break
    if all_open:
        print('All microservices are UP and LISTENING!', flush=True)
        break
    time.sleep(3)

for name, port in SERVICES:
    status = 'UP' if is_port_open(port) else 'DOWN'
    print(f'[{name}] (port {port}): {status}', flush=True)

# Keep alive indefinitely as daemon
print('Daemon supervisor active. Keeping services running...', flush=True)
while True:
    time.sleep(60)
