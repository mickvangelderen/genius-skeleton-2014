filename = '1v1v1-180rounds-hard';
data = read_tournament([filename '.csv']);

nagentsPerSession = (length(data) - 11)/2;
agents = unique([data{11 + (1:nagentsPerSession)}]);
nagents = length(agents);

utilities = cell(1, nagents);
rounds = cell(1, nagents);
pareto = cell(1, nagents);
nash = cell(1, nagents);
welfare = cell(1, nagents);

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
    
    % Compute distance to nash
    accumulator = [];
    for iaps = 1:nagentsPerSession
        indices = strcmp(data{11 + iaps}, agent);
        values = data{10}(indices);
        accumulator = [accumulator;values];
    end
    nash{iagent} = accumulator;
    
    % Compute welfare
    accumulator = [];
    for iaps = 1:nagentsPerSession
        indices = strcmp(data{11 + iaps}, agent);
        values = data{10}(indices);
        accumulator = [accumulator;values];
    end
    welfare{iagent} = accumulator;
end

agents'

means = mean(cell2mat(utilities));
stddevs = std(cell2mat(utilities));
figure('PaperPosition',  [.25 .25 8 6], 'PaperPositionMode', 'auto');
hold on
bar(1:nagents, means)
errorbar(1:nagents, means, stddevs, '.')
xlabel('Agent');
ylabel('Utility');
print([filename '-utility'], '-djpeg');
hold off

means = mean(cell2mat(rounds));
stddevs = std(double(cell2mat(rounds)));
figure('PaperPosition',  [.25 .25 8 6], 'PaperPositionMode', 'auto');
hold on
bar(1:nagents, means)
errorbar(1:nagents, means, stddevs, '.')
xlabel('Agent');
ylabel('Number of Rounds');
print([filename '-rounds'], '-djpeg');
hold off

means = mean(cell2mat(pareto));
stddevs = std(cell2mat(pareto));
figure('PaperPosition',  [.25 .25 8 6], 'PaperPositionMode', 'auto');
hold on
bar(1:nagents, means)
errorbar(1:nagents, means, stddevs, '.')
xlabel('Agent');
ylabel('Distance to Pareto Frontier');
print([filename '-pareto'], '-djpeg');
hold off

means = mean(cell2mat(nash));
stddevs = std(cell2mat(nash));
figure('PaperPosition',  [.25 .25 8 6], 'PaperPositionMode', 'auto');
hold on
bar(1:nagents, means)
errorbar(1:nagents, means, stddevs, '.')
xlabel('Agent');
ylabel('Distance to Nash');
print([filename '-nash'], '-djpeg');
hold off

means = mean(cell2mat(welfare));
stddevs = std(cell2mat(welfare));
figure('PaperPosition',  [.25 .25 8 6], 'PaperPositionMode', 'auto');
hold on
bar(1:nagents, means)
errorbar(1:nagents, means, stddevs, '.')
xlabel('Agent');
ylabel('Social Welfare');
print([filename '-welfare'], '-djpeg');
hold off
