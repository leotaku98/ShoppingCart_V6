package au.edu.sydney.soft3202.task1;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShoppingCartTest {
    String session;
    private ApplicationContext context;
    @BeforeEach
    public void init() {
        context = SpringApplication.run(ShoppingServiceApplication.class); // Literally just run our application.
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
        }catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong with our request!");
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void logout(){
        HttpRequest request;

        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/logout"))
                    .header("Cookie", session)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        SpringApplication.exit(context);

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

            return HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException | URISyntaxException e) {
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
            return HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    public void testInvalid(URI uri){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", "Not a valid session")
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertTrue(response.body().contains("Invalid template"));
        } catch (IOException | InterruptedException e) {
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

    @Test
    public void testCart() {
        try {
            //get request and success
            URI uri = new URI("http://localhost:8080/cart");
            testInvalid(uri);

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
            assertEquals(302, addItem("apple",1).statusCode());
            assertEquals(302, addItem("orange",1).statusCode());
            assertEquals(302, addItem("pear",1).statusCode());
            assertEquals(302, addItem("banana",1).statusCode());

            //get cost
            assertEquals(11.7, Double.valueOf(getCost().body()));
            assertEquals(302, addItem("apple",0).statusCode());
            assertEquals(9.2, Double.valueOf(getCost().body()));

            //test update non-exist item
            String body = "customItemCount=100&customItemName=NoExisted";
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(302, response.statusCode());//the id no exist, nothing is changed

            //test no count update
            body = "customItemCount=-100&customItemName=orange";
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(302, response.statusCode());//the count is the same, nothing is changed
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
            testInvalid(uri);

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

            //if the item is already present in cart
            body = "customItemCost=100&customItemName=apple";
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(302, response.statusCode()); //nothing happen



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
            testInvalid(uri);

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
            testInvalid(uri);

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

            //test if the name and count is null
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Cookie", session)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("updateItemName=&updateItemName=&updateItemName=&updateItemName=&updateItemCost=&updateItemCost=&updateItemCost=1&updateItemCost="))
                    .build();
            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(302, response.statusCode());

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
            testInvalid(uri);

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

            //now if we aceess newname page, we will redirect to Invalid html
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
