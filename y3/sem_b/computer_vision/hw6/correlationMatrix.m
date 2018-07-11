function [corrMat] = correlationMatrix(A, B)

    A = A+1; A(A==2) = 0;
    B = B+1; B(B==2) = 0;
    
    flippedB = rot90(B, 2);
    
    convedMat = convn(A, flippedB, 'same');
    denominator = convn(B, flippedB, 'valid');
    
    corrMat = convedMat / denominator;
    
end

