from __future__ import division, print_function
from sklearn import datasets

from k_means import Kmeans
from statistical_gap import gap_statistic as gs
import utils


def test_iris():
    iris_dat = datasets.load_iris()
    data = iris_dat.data
    k, plot_info, centers = gs(data)
    print("the returned k is : {}".format(k))
    utils.plot_info(plot_info)
    #target = iris_dat.target
    #kk = Kmeans(k, data)
    #clusters = kk.clusterize()
    #dataset_accuracy(data, target, clusters)


def test_digits():
    digits_dat = datasets.load_digits()
    data = digits_dat.data
    k, plot_info = gs(data)
    print("the returned k is : {}".format(k))
    utils.plot_info(plot_info)
    # target = digits_dat.target
    # kk = Kmeans(10, data)
    # clusters = kk.clusterize()
    # dataset_accuracy(data, target, clusters)


def test_wine():
    wine_dat = datasets.load_wine()
    data = wine_dat.data
    k, plot_info, clusters = gs(data)
    print("the returned k is : {}".format(k))
    utils.plot_info(plot_info)
    # target = wine_dat.target
    # kk = Kmeans(3, data)
    # clusters = kk.clusterize()
    # dataset_accuracy(data, target, clusters)


def five_clusters():
    exp_k = 5
    data = utils.generate_5_clusters()
    # utils.draw_3d(data)
    k, plot_info, centers = gs(data)

    print("the expected number of k is: {}".format(exp_k))
    print("the returned k is : {}".format(k))
    utils.plot_clusters(centers)
    utils.plot_info(plot_info)


def k_clusters(k=2):
    exp_k = k
    n = k * 100
    data = utils.generate_groups(n, k, utils.random_3d_point)
    k, plot_info, centers = gs(data)

    print("the expected number of k is: {}".format(exp_k))
    print("the returned k is : {}".format(k))
    utils.plot_clusters(centers)
    utils.plot_info(plot_info)


def dataset_accuracy(data, target, clusters):
    dl = map(list, data)
    dl = list(dl)
    hits = 0
    for center in clusters:
        center_ind = dl.index(list(center.features))
        center_target = target[center_ind]
        print(center_target)

        for obs in center.cluster:
            obs_ind = dl.index(list(obs.features))
            obs_target = target[obs_ind]
            if obs_target == center_target:
                hits += 1

    print("{:.2f}% success rate according to real groups.".format((hits / len(data)) * 100))

