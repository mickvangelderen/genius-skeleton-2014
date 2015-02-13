data = read_tournament('Log-Tournament_20150213-154242.csv');

nagentsPerSession = (length(data) - 11)/2;
agents = unique([data{11 + (1:nagentsPerSession)}]);
nagents = length(agents);

utilities = cell(1, nagents);
rounds = cell(1, nagents);
pareto = cell(1, nagents);

% compute gained utility for each agent
for iagent = 1:nagents
    agent = agents{iagent};
    
    % Compute utilities
    accumulator = [];
    for iaps = 1:nagentsPerSession
        indices = strcmp(data{11 + iaps}, agent);
        values = data{11 + nagentsPerSession + iaps}(indices);
        accumulator = [accumulator;values];
    end
    utilities{iagent} = accumulator;
    
    % Compute rounds
    accumulator = [];
    for iaps = 1:nagentsPerSession
        indices = strcmp(data{11 + iaps}, agent);
        values = data{3}(indices);
        accumulator = [accumulator;values];
    end
    rounds{iagent} = accumulator;
    
    % Compute distance to pareto
    accumulator = [];
    for iaps = 1:nagentsPerSession
        indices = strcmp(data{11 + iaps}, agent);
        values = data{9}(indices);
        accumulator = [accumulator;values];
    end
    pareto{iagent} = accumulator;
end

agents'

means = mean(cell2mat(utilities));
stddevs = std(cell2mat(utilities));
figure
hold on
bar(1:nagents, means)
errorbar(1:nagents, means, stddevs, '.')
hold off

means = mean(cell2mat(rounds));
stddevs = std(double(cell2mat(rounds)));
figure
hold on
bar(1:nagents, means)
errorbar(1:nagents, means, stddevs, '.')
hold off

means = mean(cell2mat(pareto));
stddevs = std(cell2mat(pareto));
figure
hold on
bar(1:nagents, means)
errorbar(1:nagents, means, stddevs, '.')
hold off