package com.example.localnow.utils;

import com.example.localnow.model.Event;
import java.util.ArrayList;
import java.util.List;

public class MockData {
    public static List<Event> getEvents() {
        List<Event> events = new ArrayList<>();

        Event e1 = new Event("송도 뮤직 페스티벌", "2025.05.03 ~ 2025.05.07", "Music", true, "2025-05-03", "2025-05-07");
        e1.setId(1);
        e1.setLat(37.3948);
        e1.setLng(126.6392);
        e1.setLocation("송도 센트럴파크");
        events.add(e1);

        Event e2 = new Event("인천 마라톤 대회", "2025.11.23", "Marathon", true, "2025-11-23", "2025-11-23");
        e2.setId(2);
        e2.setLat(37.3900);
        e2.setLng(126.6400);
        e2.setLocation("인천 문학경기장");
        events.add(e2);

        Event e3 = new Event("송도 푸드 페스티벌", "2025.06.05", "Food", true, "2025-06-05", "2025-06-05");
        e3.setId(3);
        e3.setLat(37.3920);
        e3.setLng(126.6350);
        e3.setLocation("송도 컨벤시아");
        events.add(e3);

        Event e4 = new Event("차이나타운 문화축제", "2025.07.15 ~ 2025.07.17", "Culture", true, "2025-07-15", "2025-07-17");
        e4.setId(4);
        e4.setLat(37.4756);
        e4.setLng(126.6177);
        e4.setLocation("인천 차이나타운");
        events.add(e4);

        Event e5 = new Event("월미도 불꽃축제", "2025.08.10", "Festival", true, "2025-08-10", "2025-08-10");
        e5.setId(5);
        e5.setLat(37.4756);
        e5.setLng(126.5928);
        e5.setLocation("월미도 해변");
        events.add(e5);

        return events;
    }
}
