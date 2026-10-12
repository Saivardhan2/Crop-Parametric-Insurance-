import sys
import base64

if len(sys.argv) > 2:
    target = sys.argv[1]
    data = base64.b64decode(sys.argv[2])
    with open(target, 'wb') as f:
        f.write(data)
    print('Successfully written:', target)
