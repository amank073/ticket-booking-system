package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserBookingService {
    private static final String USERS_PATH = "app/src/main/java/ticket/booking/localDB/users.json";
    private User user;
    private List<User> userList;
    private ObjectMapper objectMapper = new ObjectMapper();

    public UserBookingService(User user1) throws IOException {
        this.user = user1;
        loadUsers();
    }

    public UserBookingService() throws IOException {
        loadUsers();
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void loadUsers() throws IOException {
        File users = new File(USERS_PATH);

        if (!users.exists()) {
            userList = new ArrayList<>();
            return;
        }
        userList = objectMapper.readValue(users, new TypeReference<List<User>>() {
        });
    }

    public boolean loginUser() {
        Optional<User> foundUser = userList.stream()
                .filter(user1 ->
                        user1.getUsername().equalsIgnoreCase(user.getUsername()) &&
                                UserServiceUtil.checkPassword(
                                        user.getPassword(),
                                        user1.getHashedPassword()
                                )
                ).findFirst();


        if (foundUser.isPresent()) {
            this.user = foundUser.get();
            return true;
        }
        return false;
    }

    public Boolean signUp(User user1) {
        try {
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        File userFile = new File(USERS_PATH);
        objectMapper.writeValue(userFile, userList);
    }

    public void fetchBooking() {
        user.printTickets();
    }

    public boolean cancelBooking(String ticketId) {
        try {
            Optional<User> foundUser = userList.stream()
                    .filter(u -> u.getUsername().equals(user.getUsername()))
                    .findFirst();

            if (foundUser.isEmpty()) {
                return false;
            }

            User existingUser = foundUser.get();
            if (existingUser.getTicketsBooked() == null) {
                return false;
            }

            boolean removed = existingUser.getTicketsBooked().removeIf(ticket ->
                    ticket.getTicketId().equals(ticketId) &&
                            ticket.getUserId().equals(user.getUserId())
            );

            if (!removed) {
                return false;
            }

            this.user = existingUser;

            saveUserListToFile();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    public boolean bookTrainSeat(Train train, int row, int col) {
        try {
            List<List<Integer>> seats = train.getSeats();

            if (row < 0 || col < 0 || row >= seats.size() || col >= seats.get(0).size()) {
                return false;
            }

            if (seats.get(row).get(col) == 1) {
                return false;
            }

            seats.get(row).set(col, 1);

            Ticket ticket = new Ticket(
                    UUID.randomUUID().toString(),
                    user.getUserId(),
                    train.getStations().get(0),
                    train.getStations().get(train.getStations().size() - 1),
                    new Date(),
                    train
            );

            if (user.getTicketsBooked() == null) {
                user.setTicketsBooked(new ArrayList<>());
            }

            user.getTicketsBooked().add(ticket);

            saveUserListToFile();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
