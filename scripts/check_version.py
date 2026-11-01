#!/usr/bin/env python3
"""Keep Android's version code and the stable GitHub release tag in agreement."""
from pathlib import Path
import os
import re

values = dict(line.split('=', 1) for line in Path('version.properties').read_text().splitlines() if '=' in line)
name = values['VERSION_NAME']
match = re.fullmatch(r'(0|[1-9][0-9]{0,2})\.(0|[1-9][0-9]?)\.(0|[1-9][0-9]?)(?:-r([1-9][0-9]?))?', name)
assert match, 'Use major.minor.patch with optional -rN revision.'
major, minor, patch = map(int, match.group(1, 2, 3))
revision = int(match.group(4) or 0)
assert int(values['VERSION_CODE']) == major * 1_000_000 + minor * 10_000 + patch * 100 + revision, 'VERSION_CODE does not match VERSION_NAME.'
if os.environ.get('GITHUB_REF_TYPE') == 'tag':
    assert os.environ['GITHUB_REF_NAME'] == 'v' + name, 'Release tag does not match version.properties.'
print(f'Version {name} ({values["VERSION_CODE"]}) verified')
