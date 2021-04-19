import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    void selenideTitles() {
        getUrlsFromOrder(orderText).forEach((url) -> {
            open(url);
            String title = title();
            System.out.println(title);

            assertThat(title).isEqualTo("Цитатник Рунета");

            if ($("h1").exists()) {
                String header = $("h1").getText();
                System.out.println(header);
                assertThat(header).isEqualTo("Bash.im — Цитатник Рунета");
            }
        });
    }

    @Test
    void jsoupTitles() throws IOException {
        for (String url : getUrlsFromOrder(orderText)) {
            Document doc = Jsoup.connect(url).get();
            String title = doc.title();
            System.out.println(title);

            assertThat(title).isEqualTo("Цитатник Рунета");

            if (!doc.select("h1").isEmpty()) {
                String header = doc.select("h1").text();
                System.out.println(header);
                assertThat(header).isEqualTo("Bash.im — Цитатник Рунета");
            }
        }
    }
}
