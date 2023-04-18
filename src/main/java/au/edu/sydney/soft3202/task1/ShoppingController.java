package au.edu.sydney.soft3202.task1;


import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

    String[] users = {"Admin", "B", "C", "D", "E"};

    Database db;

    private void initNewSB(String user){
        curUser = user;
        if (!sbs.containsKey(user)){
            sbs.put(user, new ShoppingBasket());
        }
    }

    private ShoppingBasket getCurSB(){
        return sbs.get(curUser);
    }

    private void initDb() throws SQLException {
        db = new Database();
        db.addUser("Admin");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam(value = "user", defaultValue = "") String user) throws SQLException {
        //init db
        initDb();
        if (db.getUser(user) == null){
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

        if (user.equals("Admin")){
            return ResponseEntity.status(HttpStatus.FOUND).headers(headers).location(URI.create("/admincart")).build();
        }

        // Redirect to the cart page, with the session-cookie-setting headers.
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).location(URI.create("/cart")).build();
    }

    @GetMapping("/admincart")
    public String admincart(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) throws SQLException {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }

        model.addAttribute("values",db.getUsers());

        return "admincart";
    }
    @PostMapping("/admincart")
    public String rmUser(@RequestParam(value="item", required=false) List<String> itemsToRemove) throws InterruptedException, SQLException {

        ArrayList<String> removedItem = new ArrayList<>();
        if (itemsToRemove==null){
            return "redirect:/admincart";
        }
        for (String user: itemsToRemove){
            System.out.println(user);
            db.delUserUS(user);
            db.delUserSB(user);
            Thread.sleep(100);
        }
        return "redirect:/admincart";
    }

    @GetMapping("/updateusers")
    public String updateusers(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }
        return "updateusers";
    }

    @PostMapping("/updateusers")
    public String addNewUser(@RequestParam("userName") String userName) throws SQLException {
        db.addUser(userName);
        db.addUsertoSB(userName);
        return "redirect:/admincart";
    }

    @GetMapping("/logout")
    public String logout(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }
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
    public String cart(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) throws SQLException {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }
        String curUserStr = sessions.get(sessionToken);
        User curUser = db.getUserSB(curUserStr);

        model.addAttribute("user",curUser);

        return "cart";
    }

    @PostMapping("/updatecart")
    public String updateCartPost(@RequestParam(value="itemCounts", required=false) String[] itemCounts,
                                 @RequestParam(value="itemNames", required=false) String[] itemNames,
                                 @RequestParam(value="itemUsers", required=false) String[] itemUsers) {
        ShoppingBasket sb = getCurSB();
        int index = 0;
        while(index < itemNames.length){
            db.updateItemCount(Integer.valueOf(itemCounts[index]), itemNames[index],itemUsers[index]);
            index += 1;
        }
        return "redirect:/cart";
    }

    @GetMapping("/updatecart")
    public String updatecart(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) throws SQLException {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }
        List<User> users = new ArrayList<>();
        for(String user: db.getUsers()){
            User curUsr = db.getUserSB(user);
            users.add(curUsr);
        }

        model.addAttribute("users",users);

        return "updatecart";
    }

    @GetMapping("/newname")
    public String newname(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) throws SQLException {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }
        List<User> users = new ArrayList<>();
        for(String user: db.getUsers()){
            User curUsr = db.getUserSB(user);
            users.add(curUsr);
        }

        model.addAttribute("users",users);
        return "newname";
    }
    @PostMapping("/newname")
    public String addNewItem(@RequestParam("customItemUser") String itemUser,
                            @RequestParam("customItemName") String itemName,
                             @RequestParam("customItemCost") double itemCost) throws SQLException {
        db.addNewItem(itemUser, itemName.toLowerCase(), itemCost);

        return "redirect:/cart";
    }

    @GetMapping("/delname")
    public String delname(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) throws SQLException {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }
        String curUserStr = sessions.get(sessionToken);
        User curUser = db.getUserSB(curUserStr);


        model.addAttribute("user",curUser);
        return "delname";
    }

    @PostMapping("/delname")
    public String delItem(@RequestParam(value="itemName", required=false) List<String> itemNames,
                          @RequestParam(value="itemUser", required=false) List<String> itemUsers) {
        ShoppingBasket sb = getCurSB();
        ArrayList<String> removedItem = new ArrayList<>();
        for(String item: sb.names){
            removedItem.add(item);
        }
        removedItem.removeAll(itemNames);
        for(String item: removedItem){
            sb.removeProduct(item);
        }
        return "redirect:/cart";
    }

    @GetMapping("/updatename")
    public String updatename(@CookieValue(value = "session", defaultValue = "") String sessionToken, Model model) throws SQLException {
        if (!sessions.containsKey(sessionToken)) {
            return "invalid";
        }
        String curUserStr = sessions.get(sessionToken);
        User curUser = db.getUserSB(curUserStr);

        model.addAttribute("user",curUser);
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

    @GetMapping("/cost")
    public ResponseEntity<String> cost() {
        ShoppingBasket sb = getCurSB();
        return ResponseEntity.status(HttpStatus.OK).body(
                sb.getValue() == null ? "0" : sb.getValue().toString()
        );
    }
}
