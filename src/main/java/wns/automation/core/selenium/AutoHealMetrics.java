package wns.automation.core.selenium;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoHealMetrics {

    private static AutoHealMetrics instance;

    private final AtomicInteger totalHeals = new AtomicInteger(0);
    private final AtomicInteger failedHeals = new AtomicInteger(0);
    private final ConcurrentHashMap<String, AtomicInteger> pageHealCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> fallbackTypeCounts = new ConcurrentHashMap<>();
    private final List<Long> healTimes = Collections.synchronizedList(new ArrayList<Long>());

    private AutoHealMetrics() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (AutoHealConfig.getInstance().isReportEnabled()) {
                    printReport();
                }
            }
        }));
    }

    public static synchronized AutoHealMetrics getInstance() {
        if (instance == null) {
            instance = new AutoHealMetrics();
        }
        return instance;
    }

    public void recordHeal(String pageName, String fieldName, String fallbackType, long timeMs) {
        totalHeals.incrementAndGet();

        String page = (pageName != null) ? pageName : "Unknown";
        String ft = (fallbackType != null) ? fallbackType : "Unknown";

        AtomicInteger pageCount = pageHealCounts.get(page);
        if (pageCount == null) {
            pageCount = new AtomicInteger(0);
            AtomicInteger existing = pageHealCounts.putIfAbsent(page, pageCount);
            if (existing != null) pageCount = existing;
        }
        pageCount.incrementAndGet();

        AtomicInteger typeCount = fallbackTypeCounts.get(ft);
        if (typeCount == null) {
            typeCount = new AtomicInteger(0);
            AtomicInteger existing = fallbackTypeCounts.putIfAbsent(ft, typeCount);
            if (existing != null) typeCount = existing;
        }
        typeCount.incrementAndGet();

        healTimes.add(timeMs);
    }

    public void recordFailure(String pageName, String fieldName) {
        failedHeals.incrementAndGet();
    }

    public int getTotalHeals() {
        return totalHeals.get();
    }

    public int getFailedHeals() {
        return failedHeals.get();
    }

    public String getMostUnstablePage() {
        String topPage = "N/A";
        int maxCount = 0;
        for (Map.Entry<String, AtomicInteger> entry : pageHealCounts.entrySet()) {
            int count = entry.getValue().get();
            if (count > maxCount) {
                maxCount = count;
                topPage = entry.getKey();
            }
        }
        return topPage + " (" + maxCount + " heals)";
    }

    public String getMostCommonFallbackType() {
        String topType = "N/A";
        int maxCount = 0;
        for (Map.Entry<String, AtomicInteger> entry : fallbackTypeCounts.entrySet()) {
            int count = entry.getValue().get();
            if (count > maxCount) {
                maxCount = count;
                topType = entry.getKey();
            }
        }
        return topType + " (" + maxCount + " times)";
    }

    public long getAverageHealTime() {
        List<Long> times = new ArrayList<Long>(healTimes);
        if (times.isEmpty()) return 0;
        long sum = 0;
        for (long t : times) sum += t;
        return sum / times.size();
    }

    public void printReport() {
        if (totalHeals.get() == 0 && failedHeals.get() == 0) return;

        System.out.println();
        System.out.println("========== AUTO-HEAL STATISTICS ==========");
        System.out.println("Total Heals          : " + totalHeals.get());
        System.out.println("Failed Heals         : " + failedHeals.get());
        System.out.println("Most Unstable Page   : " + getMostUnstablePage());
        System.out.println("Most Common Fallback : " + getMostCommonFallbackType());
        System.out.println("Average Heal Time    : " + getAverageHealTime() + " ms");
        System.out.println("==========================================");
        System.out.println();
    }
}
