function [corrMat] = correlationMatrix(A, B)

B = crop_letter(B);
corrMat = zeros(size(A));
flippedB = rot90(B, 2);
B_corr = convn(B, flippedB, 'valid');

subMats = getSubMatrices(A,B);

[~, ~, mat_i, mat_j] = size(subMats);

for i = 1:mat_i
    for j = 1:mat_j
        mat = subMats(:,:,i,j);
        flippedMat = rot90(mat, 2);
        convedMat = convn(mat, flippedB, 'valid');
        denominator = sqrt(B_corr) * sqrt(convn(mat, flippedMat, 'valid')); 
        if denominator==0; denominator=1; end
        corrMat(i,j) = convedMat / denominator;
    end
end
end

