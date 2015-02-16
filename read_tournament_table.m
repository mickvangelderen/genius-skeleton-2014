function [ sessions, agents ] = read_tournament_table( filename )
%read_tournament_table Read tournament csv
%   Detailed explanation
    fid = fopen(filename);
    fgetl(fid); % skip sep=;
    headerLine = fgetl(fid); % skip headers
    headers = strsplit(headerLine, ';');
    nagentsPerSession = (length(headers) - 11)/2;
    format = ['%d%s%d%s%s%d%s%s%s%s%s' repmat('%s%s', 1, nagentsPerSession)];
    data = textscan(fid, format, 'Delimiter',';');
    fclose(fid);
    % convert strings with doubles where necessary
    for i = [2 7:11 11 + nagentsPerSession + (1:nagentsPerSession)];
        data{i} = str2double(strrep(data{:, i}, ',', '.'));
    end
    for i = 11 + (1:nagentsPerSession)
        for j = 1:length(data{i})
            tokens = regexp(data{i}{j}, '\.([\w\d])+@', 'tokens');
            data{i}{j} = tokens{1};
        end
    end
    % create tables
    sessions = cell2table([ ...
        num2cell(data{1}) ... % id
        num2cell(data{2}) ... % time
        num2cell(data{3}) ... % num_rounds
        data{4} ... % agreement
        data{5} ... % discounted
        num2cell(data{6}) ... % num_approved
        num2cell(data{9}) ... % pareto dist
        num2cell(data{10}) ... % nash_dist
        num2cell(data{11}) ... % welfare
    ], 'VariableNames', ...
        { 'session_id' 'time', 'num_rounds', 'agreement', 'discounted', ...
        'num_approved', 'pareto_dist', ...
        'nash_dist', 'welfare' });
    
    dataAgents = cell(size(sessions.session_id, 1), 3);
    for i = 1:nagentsPerSession
        dataAgents(:,1) = num2cell(data{1});
        dataAgents(:,2) = data{11 + i};
        dataAgents(:,3) = num2cell(data{11 + nagentsPerSession + i});
    end
    agents = cell2table(dataAgents, ...
            'VariableNames', { 'session_id', 'agent', 'utility' });
end
