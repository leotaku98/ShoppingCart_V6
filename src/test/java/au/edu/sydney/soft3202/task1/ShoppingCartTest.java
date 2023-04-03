package au.edu.sydney.soft3202.task1;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShoppingCartTest {
    String session;
    @BeforeEach
    public void init() {
        try{
            URI uri = new URI("http://localhost:8080/login");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "user=A" ))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            String header = response.headers().firstValue("Set-Cookie").orElse("");
            String[] headerArr = header.split(";");
            for (String part : headerArr) {
                if (part.contains("session")){
                    this.session = part;
                    break;
                }
            }
            System.out.println("init working now"+this.session);
        }catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong with our request!");
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    public void testLogin() {
        try {
            //test valid user-id
            URI uri = new URI("http://localhost:8080/login");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "user=A"))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(302, response.statusCode());

            //test invalid user id
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("user=BLAJSDH"))
                    .build();
            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(401, response.statusCode());
            assertEquals("Invalid user.\n", response.body());

        } catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong with our request!");
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> addItem(String name, Integer count){
        URI uri;
        try {
            String body = "customItemCount=" + count + "&customItemName=" + name;
            uri = new URI("http://localhost:8080/cart");
            HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Cookie", session)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    private HttpResponse<String> getCost(){
        try {
            URI costUri = new URI("http://localhost:8080/cost");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(costUri)
                    .header("Cookie", session)
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response status code was: " + response.statusCode());
            System.out.println("Response headers were: " + response.headers());
            System.out.println("Response body was:\n" + response.body());
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }
    @AfterEach
    public void logout(){
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/logout"))
                    .header("Cookie", session)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testCart() {
        try {
            //get request and success
            URI uri = new URI("http://localhost:8080/cart");
            URI costUri = new URI("http://localhost:8080/cost");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());

            //Test Post Method
            assertEquals(302, addItem("apple",1).statusCode());
            assertEquals(302, addItem("orange",1).statusCode());
            assertEquals(302, addItem("pear",1).statusCode());
            assertEquals(302, addItem("banana",1).statusCode());

            //get cost
            assertEquals(11.7, Double.valueOf(getCost().body()));

        } catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong with our request!");
            System.out.println(e.getMessage());
        }
        catch (URISyntaxException ignored) {
        }
    }

    @Test
    public void testNewname() {
        try {
            //get request and success
            URI uri = new URI("http://localhost:8080/newname");
            URI costUri = new URI("http://localhost:8080/cost");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());


            //Test Post Method
            String body = "customItemCost=100&customItemName=newitEm";
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(302, response.statusCode());
            addItem("newitem", 1);

            //get cost
            assertEquals(100, Double.valueOf(getCost().body()));

        } catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong with our request!");
            System.out.println(e.getMessage());
        }
        catch (URISyntaxException ignored) {
        }
    }

    @Test
    public void testDelname() {
        try {
            //get request and success
            URI uri = new URI("http://localhost:8080/delname");
            URI costUri = new URI("http://localhost:8080/cost");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            //before deleting all items
            addItem("apple",1);
            assertEquals(2.5, Double.valueOf(getCost().body()));

            //Test Post Method
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("item=off"))
                    .build();

            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(302, response.statusCode());

            //get cost

            assertEquals(0, Double.valueOf(getCost().body()));

        } catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong with our request!");
            System.out.println(e.getMessage());
        }
        catch (URISyntaxException ignored) {
        }
    }

    @Test
    public void testUpdatename() {
        try {
            //get request and success
            URI uri = new URI("http://localhost:8080/updatename");
            URI costUri = new URI("http://localhost:8080/cost");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());

            //Test Post Method
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("updateItemName=new&updateItemCost=99"))
                    .build();
            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(302, response.statusCode());

            //add new count to the new product,99
            addItem("new",1);
            //the cost should be 1 "new" product,
            assertEquals(99, Double.valueOf(getCost().body()));

        } catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong with our request!");
            System.out.println(e.getMessage());
        }
        catch (URISyntaxException ignored) {
        }
    }

    @Test
    public void testLogout() {
        try {
            //get request and success
            URI uri = new URI("http://localhost:8080/logout");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());

            //now logging out
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(302, response.statusCode());

            //now we if aceess newname page, we will redirect to Invalid html
            request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/newname"))
                    .header("Cookie", session)
                    .GET()
                    .build();
            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertTrue(response.body().contains("Invalid template"));
        } catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong with our request!");
            System.out.println(e.getMessage());
        }
        catch (URISyntaxException ignored) {
        }
    }

}
