/*
 * EdgeTime.h
 *
 *  Created on: 26 May 2017
 *      Author: noam
 */

#ifndef EDGETIME_H_
#define EDGETIME_H_

#include "Edge.h"

class EdgeTime: public Edge {
public:
	EdgeTime();
	EdgeTime(int , string );

	/* default CTORS */
	EdgeTime(const EdgeTime&)=default;
	EdgeTime( EdgeTime&&)=default;
	EdgeTime& operator=(const EdgeTime&)=default;
	EdgeTime& operator=( EdgeTime&&)=default;

    
    
    void updateEdge(const Edge&);

	~EdgeTime();

private:
	int travels_counter;
};

#endif /* EDGETIME_H_ */
