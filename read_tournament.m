function [ data ] = read_tournament( filename )
%read_tournament2 Read tournament csv for two agent runs
%   Detailed explanation
    fid = fopen(filename);
    fgetl(fid); % skip sep=;
    headerLine = fgetl(fid); % skip headers
    headers = strsplit(headerLine, ';');
    nagentsPerSession = (length(headers) - 11)/2;
    format = ['%d%s%d%s%s%d%s%s%s%s%s' repmat('%s%s', 1, nagentsPerSession)];
    data = textscan(fid, format, 'Delimiter',';');
    % convert strings with doubles where necessary
    for i = [2 7:11 11 + nagentsPerSession + (1:nagentsPerSession)];
        data{i} = str2double(strrep(data{:, i}, ',', '.'));
    end
    for i = 11 + (1:nagentsPerSession)
        for j = 1:length(data{i})
            tokens = regexp(data{i}{j}, '\.([\w\d])+@', 'tokens');
            data{i}{j} = tokens{1};
        end
        data{i} = [data{i}{:}]';
    end
    fclose(fid);
end
