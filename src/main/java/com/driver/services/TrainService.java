package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    List<Train> trains = new ArrayList<>();

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        Train train = new Train();
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        List<Station> list = trainEntryDto.getStationRoute();
        String route = "";

        for(int i=0; i<list.size(); i++) {
            if(i==list.size()-1)
                route += list.get(i);
            else
                route += list.get(i) + ",";
        }
        train.setRoute(route);

        train.setDepartureTime(trainEntryDto.getDepartureTime());
        trains.add(train);
        return trainRepository.save(train).getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        Train train  = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        List<Ticket> ticketList = train.getBookedTickets();

        String []trainRoot = train.getRoute().split(",");
        HashMap<String,Integer> map = new HashMap<>();

        for(int i=0; i<trainRoot.length; i++) {
            map.put(trainRoot[i], i);
        }
        if(!map.containsKey(seatAvailabilityEntryDto.getFromStation().toString()) || map.containsKey(seatAvailabilityEntryDto.getToStation().toString())) {
            return 0;
        }

        int booked = 0;
        for(Ticket ticket : ticketList) {
            booked+=ticket.getPassengersList().size();
        }

        int count = train.getNoOfSeats()-booked;
        for(Ticket t : ticketList) {
            String fromStation = t.getFromStation().toString();
            String toStation = t.getToStation().toString();

            if(map.get(seatAvailabilityEntryDto.getToStation().toString()) <= map.get(fromStation)) {
                count++;
            }
            else if(map.get(seatAvailabilityEntryDto.getFromStation().toString()) >= map.get(toStation)) {
                count++;
            }
        }
        return count+2;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        Train train = trainRepository.findById(trainId).get();
        String reqStation = station.toString();
        String arr[] = train.getRoute().split(",");
        boolean found = false;

        for(String s:arr) {
            if(s.equals(reqStation)) {
                found = true;
                break;
            }
        }

        if(found == false) {
            throw new Exception("Train is not passing from this station");
        }

        int noOfPassangers = 0;
        List<Ticket> ticketList = train.getBookedTickets();
        for(Ticket ticket : ticketList) {
            if(ticket.getFromStation().toString().equals(reqStation)) {
                noOfPassangers += ticket.getPassengersList().size();
            }
        }
        return noOfPassangers;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        Train train = trainRepository.findById(trainId).get();
        int age = Integer.MIN_VALUE;

        if(train.getBookedTickets().size() == 0) return 0;

        List<Ticket> ticketList = train.getBookedTickets();

        for(Ticket ticket: ticketList) {
            List<Passenger> passangers = ticket.getPassengersList();
            for(Passenger passenger : passangers) {
                age = Math.max(age, passenger.getAge());
            }
        }

        return age;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        List<Integer> TrainList = new ArrayList<>();
        List<Train> trains = trainRepository.findAll();

        for(Train t : trains) {
            String s = t.getRoute();
            String ans[] = s.split(",");

            for(int i=0; i<ans.length; i++) {
                if(Objects.equals(ans[i], String.valueOf(station))) {
                    int startTimeInMin = (startTime.getHour() * 60) + startTime.getMinute();
                    int lastTimeInMin = (endTime.getHour() * 60 ) + endTime.getMinute();

                    int departureTimeMin = (t.getDepartureTime().getHour() * 60) + t.getDepartureTime().getMinute();
                    int reachingTimeInMin = departureTimeMin + (i * 60);
                    if(reachingTimeInMin >= startTimeInMin && reachingTimeInMin <= lastTimeInMin)
                        TrainList.add(t.getTrainId());
                }
            }
        }
        return TrainList;
    }
}
