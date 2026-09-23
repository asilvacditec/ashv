package org.ash.sqlserver;

import org.ash.activity.ActivityRepository.*;
import org.ash.activity.ActivityRepository.Point;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/** Experimental SQL Server view, independent from Oracle-specific ASH assumptions. */
public final class SqlServerFrame extends JFrame {
    private final JTextField host = new JTextField("localhost", 15);
    private final JTextField port = new JTextField("1433", 5);
    private final JTextField database = new JTextField("AshViewer", 12);
    private final JTextField user = new JTextField(12);
    private final JPasswordField password = new JPasswordField(12);
    private final JCheckBox trust = new JCheckBox("Confiar no certificado do servidor (laboratorio)");
    private final JTextField from = new JTextField(Instant.now().minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS).toString(), 22);
    private final JTextField to = new JTextField(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString(), 22);
    private final JButton load = new JButton("Consultar historico");
    private final JLabel status = new JLabel("Conecte ao banco de monitoramento com o repositorio ashv instalado.");
    private final JTabbedPane tabs = new JTabbedPane();
    private final ActivityChart chart = new ActivityChart();
    private final JPanel chartPanel = new JPanel(new BorderLayout());
    private final JLabel summary = new JLabel("Sem consulta.");

    public SqlServerFrame() {
        super("ASH Viewer - SQL Server 2012+ (experimental)");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1180, 740);
        setLocationByPlatform(true);
        JPanel fields = new JPanel(new GridLayout(0, 1));
        JPanel connection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addField(connection, "Host", host); addField(connection, "Porta", port);
        addField(connection, "Banco", database); addField(connection, "Usuario", user);
        addField(connection, "Senha", password);
        fields.add(connection);
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT));
        options.add(trust); fields.add(options);
        JPanel range = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addField(range, "Inicio UTC (inclusivo)", from); addField(range, "Fim UTC (exclusivo)", to);
        JButton now = new JButton("Ultima hora");
        now.addActionListener(e -> {
            Instant end = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            from.setText(end.minus(1, ChronoUnit.HOURS).toString()); to.setText(end.toString());
        });
        range.add(now); range.add(load); fields.add(range);
        fields.add(new JLabel("Amostras instantaneas: consultas entre coletas podem nao aparecer. Contagens nao representam segundos de CPU."));
        add(fields, BorderLayout.NORTH);
        chartPanel.add(summary, BorderLayout.NORTH);
        chartPanel.add(chart, BorderLayout.CENTER);
        tabs.addTab("Atividade", chartPanel);
        add(tabs, BorderLayout.CENTER); add(status, BorderLayout.SOUTH);
        load.addActionListener(e -> load());
    }

    private static void addField(JPanel panel, String label, JComponent field) {
        panel.add(new JLabel(label)); panel.add(field);
    }

    private void load() {
        try {
            Instant start = Instant.parse(from.getText().trim()), end = Instant.parse(to.getText().trim());
            char[] secret = password.getPassword();
            com.microsoft.sqlserver.jdbc.SQLServerDataSource ds;
            try {
                ds = SqlServerConnections.dataSource(host.getText(), Integer.parseInt(port.getText()),
                        database.getText(), user.getText(), new String(secret), trust.isSelected());
            } finally { Arrays.fill(secret, '\0'); password.setText(""); }
            load.setEnabled(false);
            tabs.removeAll(); tabs.addTab("Atividade", chartPanel);
            chart.setPoints(List.of()); summary.setText("Consultando...");
            status.setText("Lendo historico; limite de 30 segundos por consulta.");
            new SwingWorker<Report, Void>() {
                @Override protected Report doInBackground() throws Exception {
                    try { return new SqlServerRepository(ds::getConnection).read(start, end); }
                    finally { ds.setPassword(""); }
                }
                @Override protected void done() {
                    load.setEnabled(true);
                    if (!isDisplayable()) return;
                    try {
                        Report report = get();
                        chart.setPoints(report.points());
                        String span = report.points().isEmpty() ? "Nenhuma coleta no intervalo (nao significa ausencia de atividade)."
                                : report.points().size() + " coletas; primeira: " + report.points().get(0).time()
                                + "; ultima: " + report.points().get(report.points().size()-1).time();
                        summary.setText(span);
                        tabs.addTab("Top esperas / estados", ranking(report.waits()));
                        tabs.addTab("Top SQL (banco / hash)", ranking(report.queries()));
                        tabs.addTab("Top sessoes (ID / login local)", ranking(report.sessions()));
                        Object[][] rows = report.requests().stream().map(r -> new Object[]{r.time(), r.session(),
                                r.loginTime(), r.request(), r.database(), r.login(), r.status(), r.waitType(),
                                r.blocker(), r.cpuMs(), r.sql()}).toArray(Object[][]::new);
                        tabs.addTab("Requisicoes", table(rows, new String[]{"UTC", "Sessao", "Login (hora do servidor)",
                                "Request", "Banco", "Usuario", "Estado", "Espera", "Bloqueador", "CPU acumulada (ms)", "SQL (ate 2000 caracteres)"}));
                        status.setText("Consulta concluida. Rankings: observacoes por requisicao; detalhes: "
                                + (report.detailsTruncated() ? "1000 mais recentes (limite atingido)." : report.requests().size() + " registros."));
                    } catch (Exception ex) {
                        summary.setText("Consulta falhou; nenhum resultado exibido.");
                        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                        status.setText("Falha ao consultar o repositorio.");
                        JTextArea message = new JTextArea(cause.getMessage(), 6, 70);
                        message.setEditable(false); message.setLineWrap(true); message.setWrapStyleWord(true);
                        JOptionPane.showMessageDialog(SqlServerFrame.this, new JScrollPane(message), "SQL Server", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Confira os campos e as datas UTC, por exemplo 2026-09-23T12:00:00Z.",
                    "Parametros invalidos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JScrollPane ranking(List<Ranking> values) {
        return table(values.stream().map(r -> new Object[]{r.name(), r.observations()}).toArray(Object[][]::new),
                new String[]{"Grupo", "Observacoes"});
    }

    private static JScrollPane table(Object[][] rows, String[] columns) {
        JTable table = new JTable(new DefaultTableModel(rows, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int column) {
                for (int row = 0; row < getRowCount(); row++) {
                    Object value = getValueAt(row, column);
                    if (value != null) return value.getClass();
                }
                return String.class;
            }
        });
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < columns.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 280 : 180);
        return new JScrollPane(table);
    }

    /** Plot observations only: never interpolate missing collections as zero activity. */
    static final class ActivityChart extends JPanel {
        private List<Point> points = List.of();
        void setPoints(List<Point> points) { this.points = points; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int left = 65, top = 40, width = Math.max(1, getWidth()-110), height = Math.max(1, getHeight()-95);
            g.setColor(Color.DARK_GRAY);
            g.drawString("Requisicoes ativas por coleta (pontos observados)", left, 20);
            g.drawLine(left, top, left, top+height); g.drawLine(left, top+height, left+width, top+height);
            if (points.isEmpty()) return;
            long max = Math.max(1, points.stream().mapToLong(Point::activeRequests).max().orElse(1));
            long first = points.get(0).time().toEpochMilli(), last = points.get(points.size()-1).time().toEpochMilli();
            g.drawString(Long.toString(max), 10, top+5); g.drawString("0", 25, top+height);
            g.drawString("UTC: " + points.get(0).time() + "  a  " + points.get(points.size()-1).time(), left, top+height+25);
            g.setColor(new Color(32, 106, 168));
            for (Point point : points) {
                int x = left + (int)((point.time().toEpochMilli()-first) * (double)width / Math.max(1, last-first));
                int y = top + height - (int)(point.activeRequests() * (double)height / max);
                g.fillOval(x-2, y-2, 5, 5);
            }
        }
    }
}
