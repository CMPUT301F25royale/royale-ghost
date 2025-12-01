package com.example.project_part_3.Events;

import android.graphics.Bitmap;

import com.example.project_part_3.Image.Image_datamap;
import com.example.project_part_3.Users.Organizer;
import com.google.firebase.firestore.Exclude; // <-- IMPORT THIS

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Represents an event with associated data. Contains various methods relating to interactions with users
 */
public class Event {

    private Boolean geolocationEnabled;

    private String id;
    private String title;
    private String description;
    private String organizerId;
    private String location;
    private String locationName;
    private String posterImageUrl;
    private Date date_open;
    private Date date_close;
    private Date eventStartAt;
    private Date eventEndAt;

    private Float price = 0F;
    private Integer capacity;
    private Long seed;
    private Long lastLotteryTs;

    private Boolean lotteryDone;
    private Image_datamap imageInfo;
    private List<String> waitlistUserIds;
    private List<String> selectedUserIds;
    private List<String> confirmedUserIds;
    private List<String> declinedUserIds;
    private List<String> alternatesUserIds;

    @Exclude
    private Organizer organizer;
    @Exclude
    private Bitmap poster;
    @Exclude
    private Date time;
    private ArrayList<String> attendant_list;
    @Exclude
    private Integer attendees;


    public Event(){
        this.waitlistUserIds = new ArrayList<>();
        this.selectedUserIds = new ArrayList<>();
        this.confirmedUserIds = new ArrayList<>();
        this.declinedUserIds = new ArrayList<>();
        this.alternatesUserIds = new ArrayList<>();
        this.attendant_list = new ArrayList<>();
        this.imageInfo = null;
        this.attendees = 0;
    }

    public Event(String title,
                 String description,
                 ArrayList<String> attendees,
                 Timestamp time,
                 Date date_open,
                 Date date_close,
                 Organizer organizer,
                 Float price,
                 String location,
                 Integer capacity,
                 Image_datamap imageInfo
                 ) {
        this(); // Calls the default constructor to init lists
        this.time = time;
        this.price = price;
        this.title = title;
        this.description = description;
        this.date_open = date_open;
        this.date_close = date_close;
        this.organizer = organizer;
        if(organizer != null) { this.organizerId = organizer.getEmail(); } // Populate the correct ID field
        this.location = location;
        this.capacity = capacity;
        this.attendant_list = (attendees != null) ? attendees : new ArrayList<>();
        this.attendees = this.attendant_list.size();
        this.eventStartAt = time;
        this.imageInfo = null;
        resetLotteryState();
    }

    public Event(String title,
                 String description,
                 ArrayList<String> attendees,
                 Timestamp time,
                 Date date_open,
                 Date date_close,
                 Organizer organizer,
                 String location,
                 Integer capacity,
                 String posterImageUrl,
                 Image_datamap imageInfo
                 ) {
        this();
        this.time = time;
        this.title = title;
        this.description = description;
        this.date_open = date_open;
        this.date_close = date_close;
        this.organizer = organizer;
        if(organizer != null) { this.organizerId = organizer.getEmail(); } // Populate the correct ID field
        this.location = location;
        this.capacity = capacity;
        this.posterImageUrl = posterImageUrl;
        this.attendant_list = (attendees != null) ? attendees : new ArrayList<>();
        this.attendees = this.attendant_list.size();
        this.eventStartAt = time;
        resetLotteryState();
        this.imageInfo = null;
    }

    public Event(
                 String organizerId,
                 String title,
                 String description,
                 String locationName,
                 String locationAddress,
                 String posterImageUrl,
                 Long registrationOpensAtMs,
                 Long registrationClosesAtMs,
                 Long eventStartAtMs,
                 Long eventEndAtMs,
                 Integer capacity,
                 Float price,
                 Boolean geolocationEnabled) {
        this();
        this.id = generateUniqueId(organizerId, title, eventStartAtMs) ;
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.locationName = locationName;
        this.location = locationAddress;
        this.posterImageUrl = posterImageUrl;
        this.date_open = new Date(registrationOpensAtMs);
        this.date_close = new Date(registrationClosesAtMs);
        this.eventStartAt = new Timestamp(eventStartAtMs);
        this.eventEndAt = (eventEndAtMs != null) ? new Timestamp(eventEndAtMs) : null;
        this.capacity = capacity;
        this.price = price;
        this.imageInfo = null;
        resetLotteryState();
        this.geolocationEnabled = geolocationEnabled;

    }

    public Event(
            String eventId,
            String organizerId,
            String title,
            String description,
            String locationName,
            String locationAddress,
            String posterImageUrl,
            Long registrationOpensAtMs,
            Long registrationClosesAtMs,
            Long eventStartAtMs,
            Long eventEndAtMs,
            Integer capacity,
            Float price,
            Boolean geolocationEnabled) {
        this();
        this.id = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.locationName = locationName;
        this.location = locationAddress;
        this.posterImageUrl = posterImageUrl;
        this.date_open = new Date(registrationOpensAtMs);
        this.date_close = new Date(registrationClosesAtMs);
        this.eventStartAt = new Timestamp(eventStartAtMs);
        this.eventEndAt = (eventEndAtMs != null) ? new Timestamp(eventEndAtMs) : null;
        this.capacity = capacity;
        this.price = price;
        this.imageInfo = null;
        resetLotteryState();
        this.geolocationEnabled = geolocationEnabled;
    }

    /**
     * generate random eventId that is unique
     * @param organizerId The ID of the organizer
     * @param title The event title
     * @param startTimeMs The start time of the event
     * @return The unique event ID
     */

    @Exclude
    private String generateUniqueId(String organizerId, String title, long startTimeMs) {
        try {
            String input = organizerId + title + startTimeMs + UUID.randomUUID().toString();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return UUID.randomUUID().toString();
        }
    }

    public Image_datamap getImageInfo(){
        return this.imageInfo;
    }

    public void setImageInfo( Image_datamap imageInfo){
        this.imageInfo = imageInfo;
    }

    public String getTitle(){ return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription(){ return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate_open(){ return date_open; }
    public void setDate_open(Date date_open) { this.date_open = date_open; }

    public Date getDate_close(){ return date_close; }
    public void setDate_close(Date date_close) { this.date_close = date_close; }

    public Float getPrice(){ return price; }
    public void setPrice(Float price) { this.price = price; }

    public String getLocation(){ return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getCapacity(){ return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Boolean getLotteryDone() { return lotteryDone; }
    public void setLotteryDone(Boolean lotteryDone) { this.lotteryDone = lotteryDone; }

    public String getId() { return id; }
    public void setId(String eventId) { this.id = eventId; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getPosterImageUrl() { return posterImageUrl; }
    public void setPosterImageUrl(String posterImageUrl) { this.posterImageUrl = posterImageUrl; }

    public Date getEventStartAt() { return (eventStartAt != null) ? eventStartAt : time; }
    public void setEventStartAt(Date ts) { this.eventStartAt = ts; }

    public Date getEventEndAt() { return eventEndAt; }
    public void setEventEndAt(Date ts) { this.eventEndAt = ts; }

    public List<String> getWaitlistUserIds() { return waitlistUserIds; }
    public void setWaitlistUserIds(List<String> list) { this.waitlistUserIds = (list != null) ? list : new ArrayList<>(); }

    @Exclude
    public Organizer getOrganizer(){ return organizer; }
    @Exclude
    public void setOrganizer(Organizer organizer) { this.organizer = organizer; }

    @Exclude
    public Bitmap getPoster(){ return poster; }
    @Exclude
    public void setPoster(Bitmap poster) { this.poster = poster; }

    @Exclude
    public Date getTime(){ return time; }
    @Exclude
    public void setTime(Date time) { this.time = time; }

    public ArrayList<String> getAttendant_list(){ return attendant_list; }
    public void setAttendant_list(ArrayList<String> list) { this.attendant_list = list; }

    public Integer getAttendees(){ return (attendant_list != null) ? attendant_list.size() : 0; }
    public void setAttendees(Integer attendees) { this.attendees = attendees; }

    public List<String> getSelectedUserIds() {
        return (selectedUserIds != null) ? selectedUserIds : new ArrayList<>();
    }

    public List<String> getDeclinedUserIds(){
        return (declinedUserIds != null) ? declinedUserIds : new ArrayList<>();
    }

    public List<String> getAlternatesUserIds() {
        return (alternatesUserIds != null) ? alternatesUserIds : new ArrayList<>();
    }



    @Exclude
    public void addAttendant(String email){
        if (attendant_list == null) attendant_list = new ArrayList<>();
        attendant_list.add(email);
        attendees = attendant_list.size();
    }

    @Exclude
    public void removeAttendant(String email){
        if (attendant_list != null) {
            attendant_list.remove(email);
            attendees = attendant_list.size();
        }
    }

    @Exclude
    public int getConfirmedCount() {
        return (confirmedUserIds != null) ? confirmedUserIds.size() : 0;
    }
    // For unit test
    public void setConfirmedUserIds(List<String> list) { this.confirmedUserIds = (list != null) ? list : new ArrayList<>(); }

    public List<String> getConfirmedUserIds() {
        return (confirmedUserIds != null) ? confirmedUserIds : new ArrayList<>();
    }

    @Exclude
    public int getRemainingCapacity() {
        int cap = (capacity != null) ? capacity : 0;
        int remaining = cap - getConfirmedCount();
        return Math.max(remaining, 0);
    }
    @Exclude
    public boolean registrationOpen() {
        Date currentDate = new Date();
        if (date_open == null || date_close == null) return false;
        boolean afterOpen = currentDate.after(date_open);
        boolean beforeClose = currentDate.before(date_close);

        return afterOpen && beforeClose;
    }

    @Exclude
    public long daysUntilClose() {
        Date currentDate = new Date();
        if (date_close == null) return 0;

        long diff = date_close.getTime() - currentDate.getTime();

        return TimeUnit.MILLISECONDS.toDays(diff);
    }

    @Exclude
    public long hoursUntilClose() {
        Date currentDate = new Date();
        if (date_close == null) return 0;
        long diff = date_close.getTime() - currentDate.getTime();
        return TimeUnit.MILLISECONDS.toHours(diff);
    }


    /**
     *
     * @return A string representing the registration status of the event.
     */
    @Exclude
    public String registrationStatus() {
        if (!registrationOpen()) {
            return "Closed";
        }

        long days = daysUntilClose();

        if (days > 1) {
            return String.format("Open (%d days until close)", days);
        }

        if (days == 1) {
            return "Open (1 day until close)";
        }

        long hours = hoursUntilClose();

        if (hours == 0) {
            return "Open (closes within 1 hour)";
        }

        return String.format("Open (closes within %d hours)", hours + 1);
    }


    public void acceptAttendant(String email) {
        confirmedUserIds.add(email);
    }

    public void declineAttendant(String email) {
        declinedUserIds.add(email);
    }

    /**
     * Resets the lottery for this event.
     */
    public void resetLotteryState() {
        waitlistUserIds = new ArrayList<>();
        selectedUserIds = new ArrayList<>();
        alternatesUserIds = new ArrayList<>();
        confirmedUserIds = new ArrayList<>();
        declinedUserIds = new ArrayList<>();
        lotteryDone = false;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("description", description);
        map.put("organizerId", organizerId);
        map.put("location", location);
        map.put("locationName", locationName);
        map.put("posterImageUrl", posterImageUrl);
        map.put("date_open", date_open);
        map.put("date_close", date_close);
        map.put("eventStartAt", eventStartAt);
        map.put("eventEndAt", eventEndAt);
        map.put("price", price);
        map.put("capacity", capacity);
        map.put("seed", seed);
        map.put("lastLotteryTs", lastLotteryTs);
        map.put("imageInfo", imageInfo);
        map.put("waitlistUserIds", waitlistUserIds);
        map.put("selectedUserIds", selectedUserIds);
        map.put("confirmedUserIds", confirmedUserIds);
        map.put("declinedUserIds", declinedUserIds);
        map.put("alternatesUserIds", alternatesUserIds);
        map.put("attendant_list", attendant_list);
        return map;
    }
    public Boolean getGeolocationEnabled() {
        return geolocationEnabled != null && geolocationEnabled;
    }

    public void setGeolocationEnabled(Boolean geolocationEnabled) {
        this.geolocationEnabled = geolocationEnabled;
    }

    public boolean isFull() {
        return getRemainingCapacity() <= 0;
    }

}

