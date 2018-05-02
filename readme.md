#지식 베이스를 활용한 음식점 챗봇
==========================
###일반 인사(SmallTalk)
>* DialogFlow SmallTalk 활용
>```
<예시>
>  > 안녕! (사용자 입력)
>  > 반가워요! (챗봇 출력)
>```


###음식점 질문
>* 형태소 분석기, 질문 분석기 활용(ETRI 인공지능 Open API 서비스)
>* 질문 형태 분류
>1. S/P/?
>```
<예시>
국대떡볶이 메뉴 알려줘 -> 국대떡볶이(S) / 메뉴(P) sparql query (Object Search)
별리달리 위치 알려줘 -> 별리달리(S) / 위치(P) sparql query (Object Search)
>```
>2. ?/P/O
>```
<예시>
근처 카페 알려줘 -> 근처(O) / 카페(O)  sparql query (Overlapped Object Search)
분위기 좋은 술집 알려줘 -> 분위기 좋은(O) / 술집(O) sparql query (Overlapped Object Search)
>```



-------------------------------
###진행 과정
***2018.05.02***
* type1(s/p/?) 처리
