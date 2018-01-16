/*
 * Router.h
 *
 *  Created on: 16 Jan 2018
 *      Author: noam
 */

#ifndef ROUTER_H_
#define ROUTER_H_

#include "Mux.h"
#include "Demux.h"


class Router {
public:
	Router(int N, int K);
	void run(int s, double p);
	unsigned int pending_packets();
	virtual ~Router();

private:
	void pull(int muxID);

	vector<Demux> input;
	vector<Mux> output;
	vector<Layer> layers;
	int N;
	int K;

};

#endif /* ROUTER_H_ */
