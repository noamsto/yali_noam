

#ifndef ELEMENTS_SHIP_H_
#define ELEMENTS_SHIP_H_

#include "MarineElement.h"
#include <deque>

class Ships_commands;


using std::string;
using std::deque;
using std::shared_ptr;

class Ship: public Marine_Element {
    
public:
    enum State {STOPPED, DOCKED, DEAD, MOVING}; // current state.

private:
    State state;
    deque<shared_ptr<Ships_commands>> command_queue;   //command queue.
    double maxVelocity;
    Scouter scouter;

    
public:
    enum Type {FREIGHTER, CRUISE_SHIP, CRUISER };   //Neccessary for ship creation.

    
	Ship(Type t, string name,Point position, double fuel,double maxV);
	virtual ~Ship()=default;
    
    virtual Type getType()const=0;

    
    //command queue manipulation.
    virtual void enqueue(Ships_commands *sc);
    
    void halt(); //Stop ship.
    virtual void advance(); //actual moving functin, needs to calculate progress and LPM
    
    
    //return next command.
    shared_ptr<Ships_commands> getNextCommand()const;
    
    void dequeue_command(); //upon finishing succsesfully.
    
    //getter and setter.
    State get_state()const {return state;};
    void set_state(State state){Ship::state=state;};

    
    //scouter getters and setters.
    void set_direction(double arg);
    void set_velocity(double v);
    void set_route(Point dest);
    double getAzimuth()const{return scouter.getAzimuth();};
    double getVelocity()const{return scouter.getVelocity();};
    double getMaxVelocity()const{return maxVelocity;};
    void set_no_destination(){scouter.setCourse();}
    Point getDestCoordinates(){return scouter.getDestCoordinates();};

    
    bool inRange(Point p,double range);
    

    
};

#endif /* SHIP_H_ */
