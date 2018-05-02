지식 베이스를 활용한 음식점 챗봇
================
> 일반 인사(SmallTalk)
> *DialogFlow SmallTalk 활용

> 음식점 질문
*형태소 분석기, 질문 분석기 활용(ETRI 인공지능 Open API 서비스)
*질문 형태 분류
1. S/P/?
<pre><code> > 국대떡볶이 메뉴 알려줘 -> 국대떡볶이(S) / 메뉴(P) sparql query (Object Search)
> 별리달리 위치 알려줘 -> 별리달리(S) / 위치(P) sparql query (Object Search)
</code></pre>
2. ?/P/O
<pre><code> > 근처 카페 알려줘 -> 근처(O) / 카페(O)  sparql query (Overlapped Object Search)
> 분위기 좋은 술집 알려줘 -> 분위기 좋은(O) / 술집(O) sparql query (Overlapped Object Search)
</code></pre>
