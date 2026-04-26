package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.services.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.*;

public class App {

    public static void main(String[] args) {

        System.out.println("Running train booking system: ");
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService;

        Train selectedTrain = null;
        boolean isLoggedIn = false;

        try {
            userBookingService = new UserBookingService();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        while (option != 7) {
            System.out.println("Choose option: ");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a seat");
            System.out.println("6. Cancel my ticket");
            System.out.println("7. Exit the app");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input, Please enter a number");
                scanner.next();
                continue;
            }

            option = scanner.nextInt();
            switch (option) {
                case 1:
                    System.out.println("Enter the username to signup: ");
                    String nameToSignup = scanner.next();
                    System.out.println("Enter the password to signup: ");
                    String passwordToSignup = scanner.next();
                    User userToSignup = new User(nameToSignup, passwordToSignup,
                            UserServiceUtil.hashPassword(passwordToSignup),
                            new ArrayList<>(), UUID.randomUUID().toString());
                    userBookingService.signUp(userToSignup);
                    break;

                case 2:
                    System.out.println("Enter the username to login: ");
                    String nameToLogin = scanner.next();

                    System.out.println("Enter the password to login: ");
                    String passwordToLogin = scanner.next();

                    User userToLogin = new User(
                            nameToLogin,
                            passwordToLogin,
                            null,
                            new ArrayList<>(),
                            null
                    );
                    userBookingService.setUser(userToLogin);
                    isLoggedIn = userBookingService.loginUser();

                    if (isLoggedIn) {
                        System.out.println("Login successful");
                    } else {
                        System.out.println("Invalid credentials");
                    }
                    break;

                case 3:
                    if (!isLoggedIn) {
                        System.out.println("Please login first");
                        break;
                    }
                    System.out.println("Fetching your bookings: ");
                    userBookingService.fetchBooking();
                    break;

                case 4:
                    System.out.println("Enter your source station: ");
                    String source = scanner.next();

                    System.out.println("Enter your destination station: ");
                    String dest = scanner.next();

                    List<Train> trains = userBookingService.getTrains(source, dest);

                    if (trains.isEmpty()) {
                        System.out.println("No trains found");
                        break;
                    }

                    int index = 1;
                    for (Train t : trains) {
                        System.out.println(index + ". Train Id: " + t.getTrainId());

                        for (Map.Entry<String, String> entry : t.getStationTimes().entrySet()) {
                            System.out.println("   Station: " + entry.getKey() +
                                    " Time: " + entry.getValue());
                        }

                        System.out.println("----------------------");
                        index++;
                    }

                    System.out.println("Select a train by typing number (1-" + trains.size() + "): ");
                    int choice = scanner.nextInt();

                    if (choice < 1 || choice > trains.size()) {
                        System.out.println("Invalid choice");
                        break;
                    }

                    selectedTrain = trains.get(choice - 1);

                    System.out.println("You selected Train: " + selectedTrain.getTrainId());

                    break;
                case 5:
                    if (!isLoggedIn) {
                        System.out.println("Please login first");
                        break;
                    }
                    if (selectedTrain == null) {
                        System.out.println("Please select a train first");
                        break;
                    }

                    System.out.println("Available seats:");

                    List<List<Integer>> seats = selectedTrain.getSeats();

                    for (List<Integer> r : seats) {
                        for (Integer val : r) {
                            System.out.print(val + " ");
                        }
                        System.out.println();
                    }

                    System.out.println("Enter row:");
                    int row = scanner.nextInt();

                    System.out.println("Enter column:");
                    int col = scanner.nextInt();

                    boolean booked = userBookingService.bookTrainSeat(selectedTrain, row, col);

                    System.out.println(booked ? "Booked" : "Seat not available");
                    break;
                case 6:
                    if (!isLoggedIn) {
                        System.out.println("Please login first");
                        break;
                    }
                    System.out.println("Enter ticket ID:");
                    String ticketId = scanner.next();

                    boolean cancelled = userBookingService.cancelBooking(ticketId);

                    System.out.println(cancelled ? "Cancelled" : "Not found");
                    break;

                default:
                    break;
            }
        }
    }
}
