/*
 * Demux.cpp
 *
 *  Created on: 16 Jan 2018
 *      Author: noam
 */

#include "Demux.h"
#include <iostream>

Demux::Demux(): layers(nullptr), curr_index(0), K(0) {}

void Demux::add_layers(vector<Layer>* layers, int i) {
	this->layers = layers;
	K = layers->size();
	layers_q.assign(K,0);
	destination.assign(K,0);
	curr_index = i%K;
}

void Demux::add_packet(int dest) {
	if(dest < 0)
		return;

	layers_q[curr_index] = K;
	destination[curr_index] = dest;
	curr_index = (curr_index+1) % K;
}

void Demux::scatter() {
	for(int i=0; i<K; i++){
		if(layers_q[i] != 0){
			layers->at(i).write_packet(destination[i]);
			layers_q[i]--;
		}
	}
}

int Demux::pending_packets() {
	int all_packets = 0;
	for (int i = 0; i < K; ++i) {
		all_packets += layers_q[i];
	}
	return all_packets;
}


