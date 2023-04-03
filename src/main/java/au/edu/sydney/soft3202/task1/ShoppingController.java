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

import java.util.*;
import java.util.Map.Entry;
import java.net.URI;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class ShoppingController {
    private final SecureRandom randomNumberGenerator = new SecureRandom();
    private final HexFormat hexFormatter = HexFormat.of();

    private final AtomicLong counter = new AtomicLong();

    HashMap<String, ShoppingBasket> sbs = new HashMap<>();

    Map<String, String> sessions = new HashMap<>();

    String curUser = "";

    String[] users = {"A", "B", "C", "D", "E"};
    private void initNewSB(String user){
        curUser = user;
        if (!sbs.containsKey(user)){
            sbs.put(user, new ShoppingBasket());
        }


    }

    private ShoppingBasket getCurSB(){
        return sbs.get(curUser);
    }
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
        initNewSB(user);

        // Redirect to the cart page, with the session-cookie-setting headers.
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).location(URI.create("/cart")).build();
    }

    @GetMapping("/logout")
    public String logout() {

        return "logout";
    }
    @PostMapping("/logout")
    public String logOut() {
        sessions.values().remove(curUser);
        sbs.remove(curUser);
        curUser = "";
        return "redirect:/";
    }

    @GetMapping("/cart")
    public String cart(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }
        ShoppingBasket sb = getCurSB();
        HashMap<String, Integer> map = new HashMap<>();
        for (Entry<String, Integer> entry : sb.getItems()) {
            map.put(entry.getKey(), entry.getValue());
        }
        model.addAttribute("values",map);

        return "cart";
    }

    @PostMapping("/cart")
    public String updateCart(@RequestParam(value="customItemCount", required=false) String[] itemCounts,
                             @RequestParam(value="customItemName", required=false) String[] itemNames) {
        ShoppingBasket sb = getCurSB();
        int index = 0;

        while(index < itemNames.length){
            if (sb.items.get(itemNames[index]) == null){
                index += 1;
                continue;
            }
            int changedCount = Integer.valueOf(itemCounts[index]) - sb.items.get(itemNames[index]) ;
            if (changedCount == 0 || Integer.valueOf(itemCounts[index]) < 0 ){
                index += 1;
                continue;
            }
            if (changedCount > 0){
                sb.addItem(itemNames[index], changedCount);
            }else{
                sb.removeItem(itemNames[index], -changedCount);
            }

            index += 1;
        }
        return "redirect:/cart";
    }

    @GetMapping("/newname")
    public String newname(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }
        return "newname";
    }
    @PostMapping("/newname")
    public String addNewItem(@RequestParam("customItemName") String itemName,
                             @RequestParam("customItemCost") double itemCost) {
        ShoppingBasket sb = getCurSB();
        if (sb.names.contains(itemName)){
            return "redirect:/cart";
        }
        sb.addNewItem(itemName.toLowerCase(),itemCost);
        return "redirect:/cart";
    }

    @GetMapping("/delname")
    public String delname(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }
        ShoppingBasket sb = getCurSB();
        model.addAttribute("values", sb.values);
        return "delname";
    }

    @PostMapping("/delname")
    public String delItem(@RequestParam(value="item", required=false) List<String> itemsToRemove) {
        ShoppingBasket sb = getCurSB();
        ArrayList<String> removedItem = new ArrayList<>();
        for(String item: sb.names){
            removedItem.add(item);
        }
        removedItem.removeAll(itemsToRemove);
        for(String item: removedItem){
            sb.removeProduct(item);
        }
        return "redirect:/cart";
    }

    @GetMapping("/updatename")
    public String updatename(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }
        ShoppingBasket sb = getCurSB();
        model.addAttribute("values", sb.values);
        return "updatename";
    }
    @PostMapping("/updatename")
    public String updateItem(@RequestParam(value="updateItemName", required=false) List<String> updateItemNames,
                             @RequestParam(value="updateItemCost", required=false) List<String> updateItemCosts) {

        ArrayList<String> names = new ArrayList<>();
        ArrayList<Double> costs = new ArrayList<>();
        ShoppingBasket sb = getCurSB();
        for (Entry<String, Double> entry : sb.values.entrySet()) {
            names.add(entry.getKey());
            costs.add(entry.getValue());
        }
        int index = 0;
        while(index < updateItemCosts.size()){
            String new_cost = updateItemCosts.get(index);
            String new_name = updateItemNames.get(index);
            String oldNameCopy = names.get(index);
            String old_name = names.get(index);
            Double old_cost = costs.get(index);
            boolean ifUpdate = false;
            if(new_cost != ""){
                old_cost = Double.valueOf(new_cost);
                ifUpdate = true;
            }
            if(new_name != ""){
                old_name = new_name;
                ifUpdate = true;
            }
            if (ifUpdate){
                sb.removeProduct(oldNameCopy);
                sb.addNewItem(old_name, old_cost);
            }
            index += 1;
        }


        return "redirect:/cart"; // Redirect to the cart page
    }

    @GetMapping("/counter")
    public ResponseEntity<String> counter() {
        counter.incrementAndGet();
        return ResponseEntity.status(HttpStatus.OK).body("[" + counter + "]");
    }

    @GetMapping("/cost")
    public ResponseEntity<String> cost() {
        ShoppingBasket sb = getCurSB();
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
