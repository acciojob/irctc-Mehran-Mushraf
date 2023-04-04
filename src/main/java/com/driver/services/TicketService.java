package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto) throws Exception {

        //Check for validity

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        int bookedSeats = 0;

        List<Ticket> booked = train.getBookedTickets();
        for(Ticket ticket : booked) {
            bookedSeats+=ticket.getPassengersList().size();
        }

        if(bookedSeats+bookTicketEntryDto.getNoOfSeats() > train.getNoOfSeats()) {
            throw new Exception("Less tickets are available");
        }

        String stations[] = train.getRoute().split(",");
        List<Passenger> passangerList = new ArrayList<>();
        List<Integer> ids = bookTicketEntryDto.getPassengerIds();

        for(int id : ids) {
            passangerList.add(passengerRepository.findById(id).get());
        }

        int x = -1;
        int y = -1;

        for(int i=0; i< stations.length; i++) {
            if(bookTicketEntryDto.getToStation().toString().equals(stations[i])) {
                y=i;
                break;
            }
        }

        if(x == -1 || y == -1 || y-x<0 ) {
            throw new Exception("Invalid stations");
        }

        Ticket ticket = new Ticket();
        ticket.setPassengersList(passangerList);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        int fair = 0;
        fair = bookTicketEntryDto.getNoOfSeats() * (y-x) * 300;

        ticket.setTotalFare(fair);
        ticket.setTrain(train);

        train.getBookedTickets().add(ticket);
        train.setNoOfSeats(train.getNoOfSeats()-bookTicketEntryDto.getNoOfSeats());

        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticket);

        trainRepository.save(train);


       return ticketRepository.save(ticket).getTicketId();

    }
}
