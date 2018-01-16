/*
 * Layer.h
 *
 *  Created on: 16 Jan 2018
 *      Author: noam
 */

#ifndef LAYER_H_
#define LAYER_H_

#include "Mux.h"


class Layer {
public:
	Layer();
	void setMux(vector<Mux>* dest);
	void write_packet(int dest);
	void scatter(int layerID);
	int get_load(int muxID);
	void open_lane(int muxID);
	int pending_packets();
	virtual ~Layer();

private:
	vector<Mux>* destination;
	int *queues;
	bool *writing;
	int N;
};

#endif /* LAYER_H_ */
