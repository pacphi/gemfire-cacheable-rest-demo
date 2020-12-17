/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package pivotal.gemfire.demo;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

@EnableCachingDefinedRegions
@SpringBootApplication
@RestController
public class DemoApplication {

    @Cacheable("bikecache")
    @RequestMapping("/search")
    public String search(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                         @RequestParam(value = "per_page", required = false, defaultValue = "25") int perPage,
                         @RequestParam(value = "location", required = true) int location,
                         @RequestParam(value = "distance", required = false, defaultValue = "10") int distance,
                         @RequestParam(value = "stolenness", required = false, defaultValue = "proximity") String stolenness) throws IOException, URISyntaxException {
        // Bike Index API
        // https://bikeindex.org/documentation/api_v3
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost("bikeindex.org")
                .setPath("/api/v3/search")
                .setParameter("page", String.valueOf(page))
                .setParameter("per_page", String.valueOf(perPage))
                .setParameter("location", String.valueOf(location))
                .setParameter("distance", String.valueOf(distance))
                .setParameter("stolenness", stolenness)
                .build();

        long start = System.currentTimeMillis();
        try (CloseableHttpResponse response = HttpClients.createDefault().execute(new HttpGet(uri))) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try (InputStream responseStream = entity.getContent()) {
                    responseStream.transferTo(byteArrayOutputStream);
                    JSONObject jsonObject = new JSONObject(byteArrayOutputStream.toString());
                    jsonObject.put("initialResponseTimeMS", System.currentTimeMillis() - start);
                    return jsonObject.toString();
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("DemoApplication.main");
        SpringApplication.run(DemoApplication.class, args);
    }
}

