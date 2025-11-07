package com.example.project_part_3.Events;

import android.graphics.Bitmap;

import com.example.project_part_3.Users.Organizer;
import com.example.project_part_3.Users.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Event {
    private String title;
    private String description;
    private Date date_open;
    private Date date_close;
    private Organizer organizer;
    private Float price = 0F;
    private String location;
    private Integer capacity;
    private Bitmap poster;
    private Date time;
    private ArrayList<User> attendant_list;
    private Integer attendees;

    // I (Liam) added these from my event class
    private String id;// required for firebase, created when added to database
    private String organizerId;

    private String locationName;     // building or venue name
    private String posterImageUrl;   //  Firebase Storage URL
    private Date eventStartAt;
    private Date eventEndAt;
    private List<String> waitlistUserIds; // Clicked enter into lottery
    private List<String> selectedUserIds; // Offered to register (won lottery)
    private List<String> confirmedUserIds; // Accepted
    private List<String> declinedUserIds; // Declined
    private List<String> alternatesUserIds; // Alternates

    private Long seed; // Kinda optional, just for reproducibility
    private Long lastLotteryTs; // millis since epoch of last draw

    public void addAttendant(User user){
        attendant_list.add(user);
        attendees++;
    }

    public void removeAttendant(User user){
        attendant_list.remove(user);
        attendees--;
    }

    public Event(){
        // required for firebase
        this.attendant_list = new ArrayList<User>();
        this.attendees = 0;
    }

    public Event(String title,
                 String description,
                 ArrayList<User> attendees,
                 Timestamp time,
                 Date date_open,
                 Date date_close,
                 Organizer organizer,
                 Float price,
                 String location,
                 Integer capacity) {
        baseInit();
        this.time = time;
        this.price = price;
        this.title = title;
        this.description = description;
        this.date_open = date_open;
        this.date_close = date_close;
        this.organizer = organizer;
        this.location = location;
        this.capacity = capacity;
        this.attendant_list = (attendees != null) ? attendees : new ArrayList<>();
        this.attendees = this.attendant_list.size();
        this.eventStartAt = time;
    }
    public Event(String title,
                 String description,
                 ArrayList<User> attendees,
                 Timestamp time,
                 Date date_open,
                 Date date_close,
                 Organizer organizer,
                 Float price,
                 String location,
                 Integer capacity,
                 Bitmap poster) {
        baseInit();
        this.time = time;
        this.price = price;
        this.title = title;
        this.description = description;
        this.date_open = date_open;
        this.date_close = date_close;
        this.organizer = organizer;
        this.location = location;
        this.capacity = capacity;
        this.poster = poster;
        this.attendant_list = (attendees != null) ? attendees : new ArrayList<>();
        this.attendees = this.attendant_list.size();
        this.eventStartAt = time;
        this.price = price;
    }

    public Event(String title,
                 String description,
                 ArrayList<User> attendees,
                 Timestamp time,
                 Date date_open,
                 Date date_close,
                 Organizer organizer,
                 String location,
                 Integer capacity,
                 Bitmap poster) {
        baseInit();
        this.time = time;
        this.title = title;
        this.description = description;
        this.date_open = date_open;
        this.date_close = date_close;
        this.organizer = organizer;
        this.location = location;
        this.capacity = capacity;
        this.poster = poster;
        this.attendant_list = (attendees != null) ? attendees : new ArrayList<>();
        this.attendees = this.attendant_list.size();
        this.eventStartAt = time;
    }

    public Event(String eventId,
                 String organizerId,
                 String title,
                 String description,
                 String locationName,
                 String locationAddress,
                 String posterImageUrl,
                 long registrationOpensAtMs,
                 long registrationClosesAtMs,
                 long eventStartAtMs,
                 Long eventEndAtMs,
                 int capacity,
                 float price) {
        baseInit();
        this.id = id;
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.locationName = locationName;
        this.posterImageUrl = posterImageUrl;
        this.date_open = new Date(registrationOpensAtMs);
        this.date_close = new Date(registrationClosesAtMs);
        this.eventStartAt = new Timestamp(eventStartAtMs);
        this.eventEndAt = (eventEndAtMs != null) ? new Timestamp(eventEndAtMs) : null;
        this.capacity = capacity;
        this.price = price;
    }

    private void baseInit() {
        this.waitlistUserIds = new ArrayList<>();
        this.selectedUserIds = new ArrayList<>();
        this.confirmedUserIds = new ArrayList<>();
        this.declinedUserIds = new ArrayList<>();
        this.alternatesUserIds = new ArrayList<>();
    }

    public String getTitle(){ return title; }
    public String getDescription(){ return description; }
    public Date getDate_open(){ return date_open; }
    public Date getDate_close(){ return date_close; }
    public Organizer getOrganizer(){ return organizer; }
    public Float getPrice(){ return price; }
    public String getLocation(){ return location; }
    public Integer getCapacity(){ return capacity; }
    public Bitmap getPoster(){ return poster; }
    public Date getTime(){ return time; }
    public ArrayList<User> getAttendant_list(){ return attendant_list; }
    public Integer getAttendees(){ return (attendant_list != null) ? attendant_list.size() : 0; }

    // ===== New getters/setters (from Model.Event) =====
    public String getId() { return id; }
    public void setId(String eventId) { this.id = eventId; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getPosterImageUrl() { return posterImageUrl; }
    public void setPosterImageUrl(String posterImageUrl) { this.posterImageUrl = posterImageUrl; }

    public long getRegistrationOpensAt() { return (date_open != null) ? date_open.getTime() : 0L; }
    public void setRegistrationOpensAt(long ms) { this.date_open = new Date(ms); }

    public long getRegistrationClosesAt() { return (date_close != null) ? date_close.getTime() : 0L; }
    public void setRegistrationClosesAt(long ms) { this.date_close = new Date(ms); }

    public Date getEventStartAt() { return (eventStartAt != null) ? eventStartAt : time; }
    public void setEventStartAt(Timestamp ts) { this.eventStartAt = ts; this.time = ts; }

    public Date getEventEndAt() { return eventEndAt; }
    public void setEventEndAt(Timestamp ts) { this.eventEndAt = ts; }

    public List<String> getWaitlistUserIds() { return waitlistUserIds; }
    public void setWaitlistUserIds(List<String> list) { this.waitlistUserIds = (list != null) ? list : new ArrayList<>(); }

    public List<String> getSelectedUserIds() { return selectedUserIds; }
    public void setSelectedUserIds(List<String> list) { this.selectedUserIds = (list != null) ? list : new ArrayList<>(); }

    public List<String> getConfirmedUserIds() { return confirmedUserIds; }
    public void setConfirmedUserIds(List<String> list) { this.confirmedUserIds = (list != null) ? list : new ArrayList<>(); }

    public List<String> getDeclinedUserIds() { return declinedUserIds; }
    public void setDeclinedUserIds(List<String> list) { this.declinedUserIds = (list != null) ? list : new ArrayList<>(); }

    public List<String> getAlternatesUserIds() { return alternatesUserIds; }
    public void setAlternatesUserIds(List<String> list) { this.alternatesUserIds = (list != null) ? list : new ArrayList<>(); }

    public Long getLastLotterySeed() { return seed; }
    public void setLastLotterySeed(Long lastLotterySeed) { this.seed = lastLotterySeed; }

    public Long getLastLotteryRunAt() { return lastLotteryTs; }
    public void setLastLotteryRunAt(Long lastLotteryRunAt) { this.lastLotteryTs = lastLotteryRunAt; }

    public void EditTitle(String title){ this.title = title; }
    public void EditDescription(String description){ this.description = description; }
    public void EditDate_open(Date date_open){ this.date_open = date_open; }
    public void EditDate_close(Date date_close){ this.date_close = date_close; }
    public void EditPrice(Float price){ this.price = price; }
    public void EditLocation(String location){ this.location = location; }
    public void EditCapacity(Integer capacity){ this.capacity = capacity; }
    public void EditPoster(Bitmap poster){ this.poster = poster; }

    // Added some helpers

    public int getConfirmedCount() {
        return (confirmedUserIds != null) ? confirmedUserIds.size() : 0;
    }

    public int getRemainingCapacity() {
        int cap = (capacity != null) ? capacity : 0;
        int remaining = cap - getConfirmedCount();
        return Math.max(remaining, 0);
    }

    public boolean isRegistrationOpen(long nowMs) {
        long open = (date_open != null) ? date_open.getTime() : Long.MIN_VALUE;
        long close = (date_close != null) ? date_close.getTime() : Long.MAX_VALUE;
        return nowMs >= open && nowMs <= close;
    }
    public boolean addConfirmedUser(String userId) {
        Objects.requireNonNull(userId, "userId");
        if (getRemainingCapacity() <= 0) return false;
        if (!confirmedUserIds.contains(userId)) {
            confirmedUserIds.add(userId);
            return true;
        }
        return false;
    }

    public void addToWaitlist(String userId) {
        Objects.requireNonNull(userId, "userId");
        if (!waitlistUserIds.contains(userId)) waitlistUserIds.add(userId);
    }

    public boolean declineUser(String userId) {
        Objects.requireNonNull(userId, "userId");
        if (!declinedUserIds.contains(userId)) {
            declinedUserIds.add(userId);
            // ensure they aren't in confirmed or selected anymore
            selectedUserIds.remove(userId);
            confirmedUserIds.remove(userId);
            return true;
        }
        return false;
    }

    public boolean registrationOpen(){
        Date currentDate = new Date();
        boolean afterOpen = currentDate.after(date_open);
        boolean beforeClose = currentDate.before(date_close);

        return afterOpen && beforeClose;
    }

    public long daysUntilClose(){
        Date currentDate = new Date();
        return date_close.getTime() - currentDate.getTime();

    }

    public String registrationStatus(){
        if (registrationOpen()){
            return "Open ("+daysUntilClose()+")";
        }
        return "Closed";
    }
}
