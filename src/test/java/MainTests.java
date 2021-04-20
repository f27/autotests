import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class MainTests {
    private final String orderText = "Open https://bash.im\nstep 1\nstep 2\nstep 3";
    private final String openUrlPatter = "Open.*?((http|https)://[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&amp;:/~+#-]*[\\w@?^=%&amp;/~+#-])?)";

    private List<String> getUrlsFromOrder(String orderText) {
        String[] steps = orderText.split("\n");
        List<String> urls = new ArrayList<>();

        for (String step : steps) {
            Pattern p = Pattern.compile(openUrlPatter);
            Matcher m = p.matcher(step);
            if (m.find())
                urls.add(m.group(1));
        }

        return urls;
    }

    private Map<String, List<String>> collectStandardElements(String url) throws IOException {
        String[] tags = new String[] {"h1", "h2", "h3", "h4", "h5", "h6"};
        Map<String, List<String>> collectedElements = new HashMap<>();
        Document doc = Jsoup.connect(url).get();

        if (!doc.title().isEmpty()) {
            List<String> titleText = new ArrayList<>();
            titleText.add(doc.title());
            collectedElements.put("title", titleText);
        }

        for (String tag : tags) {
            if (!doc.select(tag).isEmpty()) {
                List<String> headerText = new ArrayList<>();
                doc.select(tag).forEach((header) ->
                        headerText.add(header.text())
                );
                collectedElements.put(tag, headerText);
            }
        }

        return collectedElements;
    }

    @Test
    void selenideTitles() {
        getUrlsFromOrder(orderText).forEach((url) -> {
            Map<String, List<String>> expectedData = new HashMap<>();
            try {
                expectedData = collectStandardElements(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            open(url);
            System.out.println(expectedData.keySet());
            expectedData.forEach((htmlTag, dataList) ->
                    dataList.forEach((data) -> {
                        if (htmlTag.equals("title"))
                            assertThat(title()).isEqualTo(data);
                        else
                            assertThat($$(htmlTag).texts()).contains(data);
                    })
            );

        });
    }

}
