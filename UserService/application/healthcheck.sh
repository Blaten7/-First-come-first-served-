#!/bin/bash
# 헬스체크 스크립트: 환경 변수 SERVER_PORT 사용
curl --fail http://localhost:${SERVER_PORT}/health || exit 1
