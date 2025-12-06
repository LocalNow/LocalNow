import requests
import re
from datetime import datetime, timedelta
from urllib.parse import quote, unquote
from config import Config
from venue_crawler import VenueCrawler


class DataCrawler:
    def __init__(self):
        self.PUBLIC_DATA_KEY = unquote(Config.PUBLIC_DATA_KEY) # 이중 인코딩 방지
        self.NAVER_ID = Config.NAVER_CLIENT_ID
        self.NAVER_SECRET = Config.NAVER_CLIENT_SECRET
        self.KAKAO_KEY = Config.KAKAO_REST_API_KEY
        self.seen_titles = set() # 중복 제거용 (메모리)

    # [1] 카카오: 주소 좌표 변환
    def get_geo_location(self, query):
        url = "https://dapi.kakao.com/v2/local/search/keyword.json"
        headers = {"Authorization": f"KakaoAK {self.KAKAO_KEY}"}
        params = {"query": f"인천시 연수구 {query}"}
        
        try:
            resp = requests.get(url, headers=headers, params=params)
            documents = resp.json().get('documents')
            if documents:
                return {
                    "lat": float(documents[0]['y']),
                    "lng": float(documents[0]['x']),
                    "address": documents[0]['address_name'],
                    "place_name": documents[0]['place_name']
                }
        except Exception as e:
            print(f" 카카오 좌표 변환 실패: {e}")
        return None

    # [2] 공공데이터: 축제 정보 조회
    def fetch_public_festivals(self):
        url = "http://apis.data.go.kr/B551011/KorService1/searchFestival1"
        
        # 어제 날짜 계산 (YYYYMMDD)
        yesterday = (datetime.now() - timedelta(days=1)).strftime("%Y%m%d")
        
        params = {
            "serviceKey": self.PUBLIC_DATA_KEY,
            "numOfRows": 50,
            "pageNo": 1,
            "MobileOS": "ETC",
            "MobileApp": "LocalNow",
            "arrange": "A",
            "listYN": "Y",
            "areaCode": "2",
            "eventStartDate": yesterday, # 어제 날짜부터 조회
            "_type": "json"
        }
        
        results = []
        try:
            print(f" 공공데이터 조회 중... (시작일: {yesterday})")
            resp = requests.get(url, params=params)
            items = resp.json().get('response', {}).get('body', {}).get('items', {}).get('item', [])
            
            for item in items:
                title = item.get('title')
                
                # 중복 제거
                if title in self.seen_titles:
                    continue
                self.seen_titles.add(title)

                addr = item.get('addr1', '')
                if "연수구" in addr or "송도" in addr:
                    # Date Parsing
                    start_str = item.get('eventstartdate')
                    end_str = item.get('eventenddate')
                    start_date = datetime.strptime(start_str, "%Y%m%d") if start_str else None
                    end_date = datetime.strptime(end_str, "%Y%m%d") if end_str else None

                    results.append({
                        "title": title,
                        "category": "축제",
                        "date": f"{start_str}~{end_str}",
                        "start_date": start_date,
                        "end_date": end_date,
                        "location": addr,
                        "lat": float(item.get('mapy')),
                        "lng": float(item.get('mapx')),
                        "description": "공공데이터포털 제공 공식 행사",
                        "image": item.get('firstimage', ''),
                        "source": "PublicData"
                    })
        except Exception as e:
            print(f"❌ 공공데이터 조회 오류: {e}")
            try:
                print(f"응답 본문: {resp.text[:500]}")
            except:
                pass
            
        return results

    # [Helper] 텍스트에서 날짜 추출 (YYYY.MM.DD or MM.DD or MM월 DD일)
    def extract_date_range(self, text):
        current_year = datetime.now().year
        
        # 1. YYYY.MM.DD ~ YYYY.MM.DD
        match_full = re.search(r'(\d{4}\.\d{1,2}\.\d{1,2})\s*~\s*(\d{4}\.\d{1,2}\.\d{1,2})', text)
        if match_full:
            return f"{match_full.group(1)}~{match_full.group(2)}"
            
        # 2. MM.DD ~ MM.DD (현재 연도 가정)
        match_short = re.search(r'(\d{1,2}\.\d{1,2})\s*~\s*(\d{1,2}\.\d{1,2})', text)
        if match_short:
            return f"{current_year}.{match_short.group(1)}~{current_year}.{match_short.group(2)}"
            
        # 3. MM월 DD일 ~ MM월 DD일
        match_kor = re.search(r'(\d{1,2})월\s*(\d{1,2})일\s*~\s*(\d{1,2})월\s*(\d{1,2})일', text)
        if match_kor:
            return f"{current_year}.{match_kor.group(1)}.{match_kor.group(2)}~{current_year}.{match_kor.group(3)}.{match_kor.group(4)}"

        # 4. YYYY.MM.DD (단일 날짜)
        match_single = re.search(r'(\d{4}\.\d{1,2}\.\d{1,2})', text)
        if match_single:
            return match_single.group(1)
            
        # 5. MM월 DD일 (단일 날짜)
        match_kor_single = re.search(r'(\d{1,2})월\s*(\d{1,2})일', text)
        if match_kor_single:
            return f"{current_year}.{match_kor_single.group(1)}.{match_kor_single.group(2)}"
            
        return None

    # [3] 네이버 블로그: 다양한 키워드로 로컬 이벤트 검색
    def fetch_naver_blogs(self, keywords=None):
        # 키워드가 없으면 기본 추천 키워드 리스트 사용
        if keywords is None:
            keywords = [
                "송도 플리마켓", "송도 축제", "송도 페스티벌", "송도 공연", "송도 전시", "송도 박람회", "송도 행사",
                "연수구 플리마켓", "연수구 축제", "연수구 페스티벌", "연수구 공연", "연수구 전시", "연수구 박람회", "연수구 행사"
            ]

        url = "https://openapi.naver.com/v1/search/blog.json"
        headers = {
            "X-Naver-Client-Id": self.NAVER_ID,
            "X-Naver-Client-Secret": self.NAVER_SECRET
        }
        
        all_results = []
        today = datetime.now()
        
        # Known Venues (Expanded)
        known_venues = [
            "송도컨벤시아", "센트럴파크", "트리플스트리트", "현대프리미엄아울렛", "아트센터인천",
            "연수문화재단", "인천글로벌캠퍼스", "솔찬공원", "해돋이공원", "미추홀공원",
            "달빛축제공원", "인천도시역사관", "트라이보울", "G타워", "커낼워크", "송도달빛축제공원",
            "송도국제캠핑장", "인천대학교", "연세대 국제캠퍼스", "송도 센트럴파크", "송도 한옥마을"
        ]

        for keyword in keywords:
            params = {"query": keyword, "display": 10, "sort": "sim"} # 정확도순으로 변경하여 관련성 높임
            
            try:
                print(f" 네이버 블로그 검색: {keyword}")
                resp = requests.get(url, headers=headers, params=params)
                data = resp.json()
                items = data.get('items', [])
                if not items:
                    print(f"  -> 결과 없음.")
                
                for item in items:
                    raw_title = item['title']
                    clean_title = re.sub('<.+?>', '', raw_title)
                    clean_desc = re.sub('<.+?>', '', item['description'])
                    
                    # [Strict Filtering] 송도/연수구 관련 내용이 없으면 과감히 제외
                    if "송도" not in clean_title and "연수구" not in clean_title and "송도" not in clean_desc and "연수구" not in clean_desc:
                        continue

                    # [Title Cleaning] 블로그 제목 정제
                    clean_title = re.sub(r'\[.*?\]', '', clean_title)
                    suffixes = ["후기", "리뷰", "다녀왔어요", "방문기", "소식", "안내", "개최", "일정", "정보", "추천"]
                    for suffix in suffixes:
                        clean_title = clean_title.replace(suffix, "")
                    clean_title = clean_title.strip()
                    
                    # 중복 제거
                    if clean_title in self.seen_titles:
                        continue
                    
                    # 날짜 추출 (가장 중요)
                    date_str = self.extract_date_range(clean_title + " " + clean_desc)
                    
                    # [CRITICAL] 날짜를 추출하지 못하면 이벤트로 간주하지 않음 (블로그 날짜 사용 금지)
                    if not date_str:
                        continue

                    start_date = None
                    end_date = None
                    try:
                        if "~" in date_str:
                            parts = date_str.split("~")
                            start_date = datetime.strptime(parts[0].strip(), "%Y.%m.%d")
                            end_date = datetime.strptime(parts[1].strip(), "%Y.%m.%d")
                        else:
                            start_date = datetime.strptime(date_str.strip(), "%Y.%m.%d")
                            end_date = start_date
                    except:
                        continue # 날짜 파싱 실패시 제외

                    # [Future Filter] 종료일이 오늘보다 이전이면 제외 (이미 끝난 행사)
                    if end_date and end_date < today:
                        continue

                    self.seen_titles.add(clean_title)
                    
                    # 위치 추론
                    search_query = None
                    for venue in known_venues:
                        if venue in clean_title or venue in clean_desc:
                            search_query = venue
                            break
                    
                    geo = None
                    if search_query:
                        geo = self.get_geo_location(search_query)
                    else:
                        if "송도" in clean_title:
                             geo = self.get_geo_location(clean_title)

                    if not geo:
                        continue
                    
                    if "연수구" not in geo['address'] and "송도" not in geo['address']:
                        continue

                    # 카테고리 자동 분류
                    category = "기타"
                    if "마켓" in keyword: category = "플리마켓"
                    elif "축제" in keyword or "페스티벌" in keyword: category = "축제"
                    elif "버스킹" in keyword or "공연" in keyword or "콘서트" in keyword: category = "공연"
                    elif "전시" in keyword or "박람회" in keyword: category = "전시"
                    elif "팝업" in keyword: category = "전시"

                    all_results.append({
                        "title": clean_title,
                        "category": category,
                        "date": date_str, # 정확히 추출된 날짜만 사용
                        "start_date": start_date,
                        "end_date": end_date,
                        "location": geo['place_name'],
                        "lat": geo['lat'],
                        "lng": geo['lng'],
                        "description": clean_desc,
                        "image": "",
                        "source": "NaverBlog",
                        "link": item['link']
                    })
            except Exception as e:
                print(f" 검색 오류 ({keyword}): {e}")
            
        # 3. Venue Crawling (New)
        try:
            print("Starting Venue Crawling...")
            vc = VenueCrawler()
            venue_data = vc.crawl_all()
            vc.close()
            
            for item in venue_data:
                # 중복 체크
                if item['title'] not in self.seen_titles:
                    self.seen_titles.add(item['title'])
                    
                    # 좌표가 0.0이면 위치 검색 시도
                    if item['lat'] == 0.0 and item['lng'] == 0.0:
                        geo = self.get_geo_location(item['location'])
                        if geo:
                            item['lat'] = geo['lat']
                            item['lng'] = geo['lng']
                            # 위치명도 더 정확하게 업데이트
                            if item['location'] == "인천 (상세 링크 참조)":
                                item['location'] = geo['place_name']
                    
                    # [Location Filtering] Venue data is usually trusted, but check if we have coords
                    if item['lat'] != 0.0 and item['lng'] != 0.0:
                         all_results.append(item)
            print(f"Venue Crawling done. Added {len(venue_data)} items.")
        except Exception as e:
            print(f"Venue Crawling failed: {e}")

        return all_results

if __name__ == "__main__":
    crawler = DataCrawler()
    # 1. 공공데이터 수집
    print("--- 공공데이터 수집 결과 ---")
    public_data = crawler.fetch_public_festivals()
    print(f"총 {len(public_data)}건")

    # 2. 네이버 블로그 (추천 키워드 전체) 수집
    print("\n--- 네이버 블로그 수집 결과 ---")
    blog_data = crawler.fetch_naver_blogs() # 인자 없이 호출하면 추천 키워드 사용
    print(f"총 {len(blog_data)}건")