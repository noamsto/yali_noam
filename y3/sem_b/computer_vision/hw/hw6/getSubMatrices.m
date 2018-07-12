function [ sm ] = getSubMatrices(A, B)
    C = im2col(A, size(B),'sliding');
    sm = reshape(C, [size(B), (size(A)+1-size(B))]);
end

