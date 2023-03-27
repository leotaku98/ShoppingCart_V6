package au.edu.sydney.soft3202.task1;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import java.util.Map.Entry;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class ShoppingController {
    private final SecureRandom randomNumberGenerator = new SecureRandom();
    private final HexFormat hexFormatter = HexFormat.of();

    private final AtomicLong counter = new AtomicLong();
    ShoppingBasket sb = new ShoppingBasket();

    Map<String, String> sessions = new HashMap<>();

    String[] users = {"A", "B", "C", "D"};

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam(value = "user", defaultValue = "") String user) {

        // We are just checking the username, in the real world you would also check their password here
        // or authenticate the user some other way.
        if (!Arrays.asList(users).contains(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user.\n");
        }

        // Generate the session token.
        byte[] sessionTokenBytes = new byte[16];
        randomNumberGenerator.nextBytes(sessionTokenBytes);
        String sessionToken = hexFormatter.formatHex(sessionTokenBytes);

        // Store the association of the session token with the user.
        sessions.put(sessionToken, user);

        // Create HTTP headers including the instruction for the browser to store the session token in a cookie.
        String setCookieHeaderValue = String.format("session=%s; Path=/; HttpOnly; SameSite=Strict;", sessionToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", setCookieHeaderValue);

        // Redirect to the cart page, with the session-cookie-setting headers.
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).location(URI.create("/cart")).build();
    }

    @GetMapping("/cart")
    public String cart(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) {
        if (!sessions.containsKey(sessionToken)) {
            return null;
        }
        int appleValue = 0 ;
        int orangeValue = 0;
        int pearValue = 0;
        int bananaValue = 0;
        for (Entry<String, Integer> entry : sb.getItems()) {
            if (entry.getKey().equals("apple")) {
                appleValue = entry.getValue();
            }else if (entry.getKey().equals("orange")) {
                orangeValue = entry.getValue();
            }else if (entry.getKey().equals("pear")) {
                pearValue = entry.getValue();
            }else if (entry.getKey().equals("banana")) {
                bananaValue = entry.getValue();
            }
        }
        model.addAttribute("appleCount", appleValue);
        model.addAttribute("orangeCount", orangeValue);
        model.addAttribute("pearCount", pearValue);
        model.addAttribute("bananaCount", bananaValue);

        return "cart";
    }

    @PostMapping("/cart")
    public String updateCart(@RequestParam("customItemName") String customItemName,
                             @RequestParam("customItemCount") int customItemCount) {

        sb.addItem(customItemName, customItemCount);
        return "redirect:/cart";
    }

    @GetMapping("/newname")
    public String newname(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) {
        if (!sessions.containsKey(sessionToken)) {
            return null;
        }


        return "newname";
    }
    @PostMapping("/newname")
    public String addNewName(@RequestParam("customItemName") String customItemName,
                             @RequestParam("customItemCount") int customItemCount) {

        return "redirect:/cart";
    }

    @GetMapping("/counter")
    public ResponseEntity<String> counter() {
        counter.incrementAndGet();
        return ResponseEntity.status(HttpStatus.OK).body("[" + counter + "]");
    }

    @GetMapping("/cost")
    public ResponseEntity<String> cost() {
        return ResponseEntity.status(HttpStatus.OK).body(
            sb.getValue() == null ? "0" : sb.getValue().toString()
        );
    }

    @GetMapping("/greeting")
    public String greeting(
        @RequestParam(name="name", required=false, defaultValue="World") String name,
        Model model
    ) {
        model.addAttribute("name", name);
        return "greeting";
    }

}
