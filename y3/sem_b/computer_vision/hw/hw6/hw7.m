%% Computer Vision hw 7 q2-3

%% q2 a:
clear all; clc; close all;
addpath(genpath('./materials'));

txt = read_data('unknown_text.dat');
blackBox = zeros(size(txt));

figure(1); set(gcf, 'Position', [100, 100, 1100, 450]);
subplot(1,2,1);
imagesc(txt); colormap(gray(255));
subplot(1,2,2);
imagesc(blackBox); colormap(gca, 'jet'); hold on

letters = ['i', 'k', 'o', 'v', 'x'];
fileExtension = '_text.dat';


for l = letters
   file_name = sprintf('%s%s',l, fileExtension);
   letterImage = read_data(file_name);
   [Y, X] = findLetter(txt, letterImage);
   text(X, Y, l, 'Color', 'r', 'FontSize', 20);
end

hold off;

%% q2 b:

txt = read_data('unknown_text_s.dat');
blackBox = zeros(size(txt));

fprintf("If we desire to make the decision rule corrleation=1 we need to apply Italic style to the reference letters.\n")
figure(2); set(gcf, 'Position', [100, 100, 1100, 450]);
subplot(1,2,1);
imagesc(txt); colormap(gray(255));
subplot(1,2,2);
imagesc(blackBox); colormap(gca, 'jet'); hold on

for l = letters
   file_name = sprintf('%s%s',l, fileExtension);
   letterImage = read_data(file_name);
   [Y, X] = findLetter(txt, letterImage, 0.76);
   text(X, Y, l, 'Color', 'r', 'FontSize', 20);
end

hold off

%%  Used Functions  
%   function [J, I] = findLetter(mat, letter, thresh)
%
%   if nargin < 3
%       thresh = 0.99;
%   end
%   
%   cm = correlationMatrix(mat, letter);
%   [J, I] = find(cm > thresh);
%   
%   end
%   
%   
%   
%   
%   function [corrMat] = correlationMatrix(A, B)
%   
%   B = crop_letter(B);
%   corrMat = zeros(size(A));
%   flippedB = rot90(B, 2);
%   B_corr = convn(B, flippedB, 'valid');
%   
%   subMats = getSubMatrices(A,B);
%   
%   [~, ~, mat_i, mat_j] = size(subMats);
%   
%   for i = 1:mat_i
%       for j = 1:mat_j
%           mat = subMats(:,:,i,j);
%           flippedMat = rot90(mat, 2);
%           convedMat = convn(mat, flippedB, 'valid');
%           denominator = sqrt(B_corr) * sqrt(convn(mat, flippedMat, 'valid')); 
%           if denominator==0; denominator=1; end
%           corrMat(i,j) = convedMat / denominator;
%       end
%   end
%   end
%   
%   function [cropped] = crop_letter(letter)
%   
%   horizon = sum(letter);
%   h_indices = find(horizon~=0);
%   h_start = h_indices(1);
%   h_end = h_indices(end);
%   
%   vertical = sum(letter,2);
%   v_indices = find(vertical~=0);
%   v_start = v_indices(1);
%   v_end = v_indices(end);
%   
%   cropped = letter(v_start:v_end, h_start:h_end);
%   end
%   
%   function [ sm ] = getSubMatrices(A, B)
%       C = im2col(A, size(B),'sliding');
%       sm = reshape(C, [size(B), (size(A)+1-size(B))]);
%   end
