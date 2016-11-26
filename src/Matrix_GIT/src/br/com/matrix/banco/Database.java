package br.com.matrix.banco;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import br.com.matrix.aplicacao.Armazenavel;
import br.com.matrix.aplicacao.KeyVal;
import br.com.matrix.aplicacao.ParametroEntrada;
import br.com.matrix.banco.tabelas.Datas;
import br.com.matrix.banco.tabelas.Estruturas;
import br.com.matrix.banco.tabelas.Frases;
import br.com.matrix.banco.tabelas.Frases_has_Palavras;
import br.com.matrix.banco.tabelas.Palavras;
import br.com.matrix.banco.tabelas.Pontuacoes;
import br.com.matrix.banco.tabelas.Sugestores;
import br.com.matrix.banco.tabelas.Sugestores_has_Estruturas;
import br.com.matrix.banco.tabelas.Sugestores_has_Sugestores;
import br.com.matrix.banco.tabelas.Textos;
import br.com.matrix.banco.tabelas.Textos_has_Datas;
import br.com.matrix.banco.tabelas.Textos_has_Frases;
import br.com.matrix.banco.tabelas.Tipo_Estruturas;
import br.com.matrix.banco.tabelas.classesAbstratas.ATabela;
import br.com.matrix.banco.tabelas.interfaces.IArmazenavel;
import br.com.matrix.banco.tabelas.interfaces.ICampo;
import br.com.matrix.banco.tabelas.interfaces.ILinha;
import br.com.matrix.banco.tabelas.interfaces.ITabela;
import br.com.matrix.banco.tabelas.propTabelas.Campo;
import br.com.matrix.banco.tabelas.propTabelas.ColunaFk;
import br.com.matrix.banco.tabelas.propTabelas.GenColuna;
import br.com.matrix.banco.tabelas.propTabelas.Linha;

public final class Database {

	private static Connection con = null;

	private static Statement stm = null;

	private static int sessionId = 0;

	public static void setSessionId(int id) {
		sessionId = id;
	}

	public static int getSessionId() {
		return sessionId;
	}

	/**
	 * Faz conex�o com o banco usando JDBC
	 */
	private static void connect() {

		try {

			if (con == null || con.isClosed()) {

				Class<?> driverClass = Class.forName("com.mysql.jdbc.Driver");
				Driver driver = (Driver) driverClass.newInstance();
				DriverManager.registerDriver(driver);
				con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/matrix", "root", "");
			}
			stm = con.createStatement();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Pega o id de uma tabela onde o registro cumpre determinada regra
	 * 
	 * @param table
	 *            - tabela de onde ser� extraido o id
	 * @param fieldCompare
	 *            - campo para aplica��o da regra
	 * @param value
	 *            - campo para compara��o com o campo da regra
	 * @return
	 * @throws SQLException
	 */
	public static int getId(String table, String fieldCompare, Object value) throws SQLException {
		ResultSet set = stm
				.executeQuery("SELECT id FROM " + table + " WHERE " + fieldCompare + " = " + formatParameter(value));

		if (set.next())
			return Integer.parseInt(set.getString("id"));

		return 0;

	}

	/**
	 * Pega um campo requisitado de uma tabela onde o registro cumpre
	 * determinada regra
	 * 
	 * @param table
	 *            - tabela de onde ser� extraido o id
	 * @param fieldWanted
	 *            - O campo requisitado
	 * @param fieldCompare
	 *            - campo para aplica��o da regra
	 * 
	 * @param value
	 *            - campo para compara��o com o campo da regra
	 * @return
	 * @throws SQLException
	 */
	public static int getId(String table, String fieldWanted, String fieldCompare, Object value) throws SQLException {
		ResultSet set = stm.executeQuery(
				"SELECT " + fieldWanted + " FROM " + table + " WHERE " + fieldCompare + " = " + formatParameter(value));

		if (set.next())
			return Integer.parseInt(set.getString(fieldWanted));

		return 0;

	}

	/**
	 * Express�o lambda gen�rica para filtrar listas
	 * 
	 * @param list
	 * @param pred
	 * @return
	 */
	private static <T> List<T> filter(List<T> list, Predicate<T> pred) {
		List<T> result = new ArrayList<T>();
		for (T t : list)
			if (pred.test(t))
				result.add(t);

		return result;

	}

	/**
	 * Insere os registros da Interface Armazenavel no banco, ela para ap�s 4
	 * erros
	 * 
	 * @param arm
	 */
	public static void update(IArmazenavel arm) {
		try {
			if (con == null || con.isClosed())
				connect();
		} catch (SQLException ex) {
			update(arm, 1);
			return;
		}

	}

	/**
	 * Insere os registros da Interface Armazenavel no banco, ela para ap�s 4
	 * erros
	 * 
	 * @param arm
	 * @param qt
	 * 
	 */
	public static void update(IArmazenavel arm, int qt) {
		if (qt > 3) {
			System.out.println("Verifique se o Wamp Server est� ligado");
			return;
		}
		try {
			if (con == null || con.isClosed())
				connect();

			List<ICampo> armVals = new ArrayList<>(arm.getValoresCampo().values());
			List<ICampo> palavras = new ArrayList<>();
			List<ICampo> pontuacoes = new ArrayList<>();

			pontuacoes = filter(armVals, x -> x.getColuna().getTb().getClass().equals(Pontuacoes.get().getClass()));
			palavras = filter(armVals, x -> x.getColuna().getTb().getClass().equals(Palavras.get().getClass()));

			for (ICampo pont : pontuacoes)
				if (!campoExists(pont))
					campoInsert(pont, true);
			for (ICampo palavra : palavras)
				if (!campoExists(palavra))
					checkPontuacoes(palavra);
			checkFrases(palavras);
		} catch (Exception ex) {
			update(arm, ++qt);
			System.out.println("Ocorreu um erro:" + ex.getMessage());
			return;
		} finally {
			resetCon();
		}
	}

	/**
	 * Checa se a palavra possui algum tipo de pontua��o e insere com ou sem a
	 * fk da pontua��o
	 * 
	 * @param palavra
	 */
	private static void checkPontuacoes(ICampo palavra) {
		try {

			ResultSet set = stm.executeQuery("SELECT * FROM pontuacoes");

			List<String> pontuacoes = new ArrayList<>();
			List<Integer> ids = new ArrayList<>();

			while (set.next()) {
				pontuacoes.add(set.getString(2));
				ids.add(set.getInt(1));
			}
			for (String pontuacao : pontuacoes)
				if (palavra.getValor().toString().contains(pontuacao) && !campoExists(palavra))
					campoInsert(palavra, ids.get(pontuacoes.indexOf(pontuacao)), true);

		} catch (SQLException ex) {
			System.out.println("Falha ao cadastrar");
		}

	}

	/**
	 * Insere as frases e as referencias em Frases_has_palavras
	 * 
	 * @param palavras
	 */
	private static void checkFrases(List<ICampo> palavras) {
		List<Integer> palavrasIds = new ArrayList<>();
		boolean fraseNova = false;

		for (ICampo palavra : palavras)
			if (campoExists(palavra))
				palavrasIds.add(getId(palavra));
			else
				palavrasIds.add(campoInsert(palavra, true));

		for (ICampo palavra : palavras) {

			if (!existsCompare(new Campo(new GenColuna("Id", Palavras.get()), getId(palavra)),
					new Campo(new GenColuna("id_p", Frases_has_Palavras.get()), palavra.getValor()))) {
				fraseNova = true;
			} else {
				fraseNova = false;
				break;
			}
		}
		if (fraseNova) {
			int fraseId = campoInsert(new Campo(new GenColuna("qt", Frases.get()), 1), true);

			for (int id : palavrasIds)
				campoInsert(new Campo(new GenColuna("id_f", Frases_has_Palavras.get()), fraseId), id, true);

			frasesSessionBind(fraseId);

		} else {
			int fraseId = getFraseByPalavras(new Campo(new GenColuna("id_p", Frases_has_Palavras.get()), 1),
					palavrasIds);
			increment(new Campo(new GenColuna("qt", Frases.get()), fraseId));

		}

	}

	/**
	 * Inser��o especifica para
	 * 
	 * @param fraseId
	 */
	private static void frasesSessionBind(int fraseId) {
		connect();
		try {
			stm.executeUpdate("INSERT INTO Textos_has_frases VALUES(NULL, " + getSessionId() + "," + fraseId + ")");
		} catch (SQLException e) {

		}
	}

	/**
	 * Incrementa 1 no valor da coluna a qual o campo em quest�o refer�ncia
	 * 
	 * @param campo
	 */
	private static void increment(ICampo campo) {
		connect();
		try {
			stm.executeQuery("UPDATE " + campo.getColuna().getTb().getNm() + " SET " + campo.getColuna().getNm()
					+ " = (" + campo.getColuna().getNm() + "+ 1) WHERE id = " + campo.getValor());
		} catch (SQLException e) {

		}
	}

	/**
	 * Compara dois campos no lado do banco
	 * 
	 * @param campo
	 * @param campoComparado
	 * @returnf
	 */
	private static boolean existsCompare(ICampo campo, ICampo campoComparado) {
		connect();

		try {
			ResultSet set = stm.executeQuery("SELECT * FROM " + campoComparado.getColuna().getTb().getNm() + " WHERE "
					+ campoComparado.getColuna().getNm() + " = " + campo.getValor());
			return set.next();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return false;
	}

	/**
	 * Verifica as liga��es de N : N da tabela Frases_Has_Palavras, pega o Id de
	 * uma frase que contenha toda a sequencia de ids das palavras
	 * 
	 * @param campo
	 * @param palavrasIds
	 * @return id da Frase
	 */
	private static int getFraseByPalavras(ICampo campo, List<Integer> palavrasIds) {
		connect();

		List<KeyVal> frasesPalavras = new ArrayList<>();
		List<Integer> jaFoi = new ArrayList<>();

		for (final Integer value : palavrasIds) {
			StringBuilder sb = new StringBuilder("SELECT * FROM ");

			sb.append(campo.getColuna().getTb().getNm());
			sb.append(" WHERE ");

			sb.append(campo.getColuna().getTb().getNm() + "." + campo.getColuna().getNm());
			sb.append(" = ");
			sb.append(value.toString());
			// sb.append(" AND ");

			String cmd = sb.toString();// .substring(0, sb.length() - 5);

			try {

				ResultSet set = stm.executeQuery(cmd);

				while (set.next()) {

					int val = Integer.parseInt(set.getString("id_f"));

					KeyVal k = new KeyVal(val);

					k.getVal().add(Integer.parseInt(set.getString("id_p")));

					if (!frasesPalavras.contains(k.getKey())) {
						frasesPalavras.add(k);
						jaFoi.add(k.getKey());
					} else
						frasesPalavras
								.get(frasesPalavras.indexOf(filter(frasesPalavras, x -> x.getKey() == val).get(0)))
								.getVal().add(val);
				}

			} catch (SQLException ex) {
				System.out.println("Ocorreu um erro na pesquisa");
				System.out.println(ex.getMessage());
			}
		}

		for (KeyVal frasePalavras : frasesPalavras)
			if (frasePalavras.getVal().containsAll(palavrasIds))
				return frasePalavras.getKey();

		return 0;

	}

	/**
	 * 
	 * @param campo
	 * @return O id do campo em quest�o
	 */
	private static int getId(ICampo campo) {
		try {
			ResultSet set = stm.executeQuery("SELECT Id FROM " + campo.getColuna().getTb().getNm() + " WHERE "
					+ campo.getColuna().getNm() + " = " + formatParameter(campo.getValor()));

			if (set.next())
				return Integer.parseInt(set.getString("Id"));

		} catch (SQLException ex) {
			System.out.println("Ocorreu um erro na pesquisa");
			System.out.println(ex.getMessage());
		}
		return 0;
	}

	/**
	 * 
	 * @param campo
	 * @return se o campo existe ou n�o
	 */
	private static boolean campoExists(ICampo campo) {
		connect();

		ResultSet s;
		try {
			s = stm.executeQuery("SELECT * FROM " + campo.getColuna().getTb().getNm() + " WHERE "
					+ campo.getColuna().getNm() + " LIKE " + formatParameter(campo.getValor()));

			return s.next();
		} catch (SQLException e) {
			return false;
		}

	}

	/**
	 * 
	 * @param campo
	 * @param fk
	 *            - Fk de uma tabela de refer�ncia
	 * @param ai
	 *            - Op��o utilizar funcionalidade de auto incremento
	 * @return id do registro gerado
	 */
	private static int campoInsert(ICampo campo, int fk, boolean ai) {
		connect();
		try {
			String cmd = "INSERT INTO " + campo.getColuna().getTb().getNm();

			cmd += " VALUES(" + ((ai) ? "NULL," : "");
			cmd += formatParameter(campo.getValor());
			cmd += ", " + fk + " )";

			stm.executeUpdate(cmd, Statement.RETURN_GENERATED_KEYS);

			try (ResultSet generatedKeys = stm.getGeneratedKeys()) {
				if (generatedKeys.next())
					return (int) generatedKeys.getLong(1);
				else
					throw new SQLException("Falha na inser��o, n�o houve ID obtido.");

			}
		} catch (SQLException ex) {
			System.out.println("N�o foi poss�vel inserir o valor : " + campo.getValor() + " no campo "
					+ campo.getColuna().getNm() + " da tabela :" + campo.getColuna().getTb().getNm());
			System.out.println("Pois o comando gerou a exce��o" + ex.getMessage());
			System.out.println("Portanto o retorno de Id ser� 0");

		}
		return 0;

	}

	/**
	 * 
	 * @param campo
	 * @param ai
	 *            - Op��o utilizar funcionalidade de auto incremento
	 * @return id do registro gerado
	 */
	private static int campoInsert(ICampo campo, boolean ai) {
		connect();
		try {
			String cmd = "INSERT INTO " + campo.getColuna().getTb().getNm();

			cmd += " VALUES(" + ((ai) ? "NULL," : "") + formatParameter(campo.getValor()) + " )";

			stm.executeUpdate(cmd, Statement.RETURN_GENERATED_KEYS);

			try (ResultSet generatedKeys = stm.getGeneratedKeys()) {
				if (generatedKeys.next())
					return (int) generatedKeys.getLong(1);
				else
					throw new SQLException("Falha na inser��o, n�o houve ID obtido.");

			}
		} catch (SQLException ex) {
			System.out.println("N�o foi poss�vel inserir o valor : " + campo.getValor() + " no campo "
					+ campo.getColuna().getNm() + " da tabela :" + campo.getColuna().getTb().getNm());
			System.out.println("Pois o comando gerou a exce��o" + ex.getMessage());
			System.out.println("Portanto o retorno de Id ser� 0");

		}
		return 0;

	}

	/**
	 * Pega um parametro de entrada e espalha ele para inser��o no banco, ele �
	 * dividido em palavras, frases e pontua��es, caso um registro com isso j�
	 * exista � aumentada a quantidade de usos no banco
	 * 
	 * @param e
	 *            - Valor do campo de texto que entrar� no banco
	 */
	public static void update(ParametroEntrada e) {
		ParametroEntrada entrada = new ParametroEntrada(e.getAll().substring(0, e.getAll().lastIndexOf(' ')));
		connect();

		System.out.println("started");

		List<String> palavras = new ArrayList<>(Arrays.asList(entrada.getAll().split("\\s+")));
		List<String> pontuacoes = new ArrayList<>(Arrays.asList(entrada.getAll().split("[\\w\\s]+")));

		List<ICampo> campos = new ArrayList<>();

		for (String palavra : palavras)
			if (palavra != "")
				campos.add(new Campo(new GenColuna("ds", Palavras.get()), palavra));

		for (String pontuacao : pontuacoes)
			if (pontuacao != "")
				campos.add(new Campo(new GenColuna("ds", Pontuacoes.get()), pontuacao));

		update(new Armazenavel(campos), 0);

	}

	/**
	 * Inser��o no banco especifica para o Id de sess�o
	 * 
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static int sessionInsert(int id) throws SQLException {
		connect();

		try {

			String cmd = "INSERT INTO Textos(id) VALUES(" + id + ")";

			stm.executeUpdate(cmd, Statement.RETURN_GENERATED_KEYS);

			try (ResultSet generatedKeys = stm.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					resetCon();
					return (int) generatedKeys.getLong(1);
				} else
					throw new SQLException("Falha na inser��o, n�o houve ID obtido.");

			}
		} catch (SQLException ex) {
			if (ex.getMessage().contains("ID obtido.")) {
				int dataId = getId("Textos", "Id", id);
				if (Database.getSessionId() == dataId) {
					resetCon();
					return Database.getSessionId();
				}

			}

		}
		resetCon();
		return 0;
	}

	/**
	 * Gera comando de insert e executa no banco de dados, retornando o Id
	 * gerado pelo banco
	 * 
	 * @param campos
	 * @return Id da linha inserida, gerado pelo banco
	 * @throws SQLException
	 */
	public static Integer simpleInsert(List<ICampo> campos) throws SQLException {
		connect();

		if (campos == null || campos.size() == 0)
			return null;

		String cmd = "";

		campos.removeIf(x -> x.getValor() == null || x.getValor().equals(""));

		StringBuilder sb = new StringBuilder(
				"INSERT INTO " + campos.get(0).getColuna().getTb().getNm() + " Values(null, ");

		campos.forEach(x -> sb.append(formatParameter(x.getValor() + ",")));

		cmd = sb.toString().substring(0, sb.length() - 1) + ")";

		stm.executeUpdate(cmd, Statement.RETURN_GENERATED_KEYS);

		try (ResultSet generatedKeys = stm.getGeneratedKeys()) {
			if (generatedKeys.next())
				return (int) generatedKeys.getLong(1);
			else
				throw new SQLException("Falha na inser��o, n�o houve ID obtido.");

		}
	}

	/**
	 * Gera comando de insert e executa no banco de dados, retornando o Id
	 * gerado pelo banco
	 * 
	 * @param table
	 * @param values
	 * @return Id da linha inserida, gerado pelo banco
	 * @throws SQLException
	 */
	public static int simpleInsert(String table, Object[] values) throws SQLException {
		connect();

		StringBuilder sb = new StringBuilder("INSERT INTO " + table + " VALUES( null, ");
		// O null se deve ao fato da coluna ser AI

		Arrays.asList(values).forEach(x -> sb.append(formatParameter(x) + ","));

		String cmd = sb.toString().substring(0, sb.length() - 1) + ")";

		stm.executeUpdate(cmd, Statement.RETURN_GENERATED_KEYS);

		try (ResultSet generatedKeys = stm.getGeneratedKeys()) {
			if (generatedKeys.next())
				return (int) generatedKeys.getLong(1);
			else
				throw new SQLException("Falha na inser��o, n�o houve ID obtido.");

		}
	}

	/**
	 * 
	 * @paramReq colunas - colunas
	 * @return SELECT de SQL pronto para ser executado
	 */
	public static String selectBuilder(List<GenColuna> colunas) {

		StringBuilder cmd = new StringBuilder("SELECT ");

		for (GenColuna c : colunas) {

			if (colunas.indexOf(c) > 0)
				cmd.append(",");

			cmd.append(c.getTb().getApelido() + "." + c.getNm());
		}

		cmd.append(" FROM ");

		cmd.append(buildInner(colunas));

		return cmd.toString();
	}

	/**
	 * 
	 * @paramReq colunas
	 * @return constroi a parte de Inner Join do comando SQL
	 */
	private static String buildInner(List<GenColuna> colunas) {

		List<ITabela> tbsInner = new ArrayList<>();
		List<ITabela> tbsNoInner = new ArrayList<>();
		StringBuilder inner = new StringBuilder();
		StringBuilder notInner = new StringBuilder();

		for (GenColuna c : colunas) {
			if (!tbsNoInner.contains(c.getTb()))
				tbsNoInner.add(c.getTb());
		}

		for (GenColuna coluna : colunas) {
			if (coluna instanceof ColunaFk) {

				ColunaFk cfk = (ColunaFk) coluna;

				if ((tbsNoInner.contains(cfk.getColunaRef().getTb()) || tbsInner.contains(cfk.getColunaRef().getTb()))
						&& (tbsNoInner.contains(cfk.getTb()) || tbsInner.contains(cfk.getTb()))) {

					tbsInner.remove(cfk.getColunaRef().getTb());
					tbsInner.remove(cfk.getTb());

					tbsInner.add(cfk.getColunaRef().getTb());
					tbsInner.add(cfk.getTb());

					tbsNoInner.remove(cfk.getColunaRef().getTb());
					tbsNoInner.remove(cfk.getTb());

				}

			}
		}

		for (ITabela t : tbsNoInner) {
			if (tbsNoInner.indexOf(t) > 0)
				notInner.append(", ");
			notInner.append(t.getNm() + " " + t.getApelido());
		}
		inner.append(appendCondicional(colunas, tbsInner, inner));

		String result = notInner.toString() + (inner.toString().equals("") ? "" : ", ") + inner.toString();
		return result;

	}

	private static String appendCondicional(List<GenColuna> colunas, List<ITabela> tbsInner, StringBuilder inner) {
		boolean b = false;
		for (GenColuna c : colunas) {
			if (c instanceof ColunaFk && tbsInner.contains(((ColunaFk) c).getColunaRef().getTb())) {
				ColunaFk fk = (ColunaFk) c;
				tbsInner.remove(fk.getColunaRef().getTb());
				ITabela t = fk.getColunaRef().getTb();
				if (b)
					inner.append(" inner join ");
				inner.append(t.getNm() + " " + t.getApelido());
				if (b)
					inner.append(" on " + fk.getApNm() + " = " + fk.getColunaRef().getApNm());
				b = true;
			}
			if (c instanceof ColunaFk && tbsInner.contains(c.getTb())) {
				ColunaFk fk = (ColunaFk) c;
				tbsInner.remove(fk.getTb());
				ITabela t = fk.getTb();
				if (b)
					inner.append(" inner join ");
				inner.append(t.getNm() + " " + t.getApelido());
				if (b)
					inner.append(" on " + fk.getApNm() + " = " + fk.getColunaRef().getApNm());
				b = true;
			}
		}
		return inner.toString();
	}

	/**
	 * 
	 * @paramReq cmd
	 * @return arraylist com "apar�ncia" de uma tabela
	 */

	public static List<ILinha> execute(String cmd) {
		resetCon();

		// Lista<Lista<NumeroLinha,Lista<NomeColuna,ValorColuna>>>
		List<ILinha> linhas = new ArrayList<>();

		try {

			ResultSet result = con.prepareStatement(cmd).executeQuery();

			// System.out.println("executando: " + cmd + " ...");

			int i = 0;

			while (result != null && result.next()) {

				i++;

				Linha li = new Linha();

				for (int j = 1; j <= result.getMetaData().getColumnCount(); j++) {

					// pega o tipo da coluna
					int type = result.getMetaData().getColumnType(j);

					String label = (result.getMetaData().getColumnLabel(j) != null)
							? result.getMetaData().getColumnLabel(j) : "null";

					GenColuna c = new GenColuna(label, getTbNome(result, j));

					Campo cp = null;

					// compara o tipo pego com um ENUM
					if (type == Types.VARCHAR)
						cp = new Campo(c, result.getString(j));

					else if (type == Types.DECIMAL)
						cp = new Campo(c, result.getDouble(j));

					else if (type == Types.BOOLEAN || type == Types.BIT || type == Types.BINARY)
						cp = new Campo(c, result.getBoolean(j));

					else if (type == Types.INTEGER)
						cp = new Campo(c, result.getInt(j));

					else if (type == Types.DATE)
						cp = new Campo(c, result.getDate(j));

					else if (type == Types.TIMESTAMP)
						cp = new Campo(c, result.getTimestamp(j));

					else if (type == Types.NULL)
						cp = new Campo(c, null);

					else {
						System.out.println("O camando foi parado pois no banco h� valores incompativeis");
						System.out.println("Resultado: n� " + i);
						System.out.println("Coluna :" + label);
					}

					if (cp != null)
						li.getCampos().add(cp);// Adiciona na linha

				}

				linhas.add(li);

			}
			System.out.println("Sucesso!!!");
			System.out.println(i + " resultado(s)!");

		} catch (SQLException e) {
			System.out.println("Ocorreu um erro no comando :" + cmd);
			System.out.println(e.getMessage());
			con = null;
			stm = null;
			connect();

		}

		return linhas;

	}

	/**
	 * Reinicia a conex�o por conta de erros, ou no encerramento de m�todos
	 * especificos
	 */
	private static void resetCon() {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stm != null && !stm.isClosed()) {
				stm.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		stm = null;
		con = null;
		connect();

	}

	private static ATabela getTbNome(ResultSet rs, int i) {
		try {
			if (rs.getMetaData().getTableName(i).toLowerCase().contains("o_estruturas"))
				return Tipo_Estruturas.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("_estruturas"))
				return Sugestores_has_Estruturas.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("estruturas"))
				return Estruturas.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("has_palavras"))
				return Frases_has_Palavras.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("palavras"))
				return Palavras.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("palavras"))
				return Palavras.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("has_datas"))
				return Textos_has_Datas.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("data"))
				return Datas.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("frases_has"))
				return Frases_has_Palavras.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("has_frases"))
				return Textos_has_Frases.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("frases"))
				return Frases.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("pontua"))
				return Pontuacoes.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("_sugestores"))
				return Sugestores_has_Sugestores.get();

			else if (rs.getMetaData().getTableName(i).toLowerCase().contains("sugestores"))
				return Sugestores.get();

			else
				return Textos.get();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Formata os parametros para um inser��o adequada no banco de dados
	 * 
	 * @paramReq parameter
	 * @return parametro Formatado ou em forma de string
	 * 
	 * 
	 */
	private static String formatParameter(Object parameter) {

		if (parameter == null)
			return "NULL";
		else {
			if (parameter instanceof String)
				return "'" + ((String) parameter).replace("'", "''") + "'";

			else if (parameter instanceof Timestamp)
				return "to_timestamp('" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS").format(parameter)
						+ "', 'mm/dd/yyyy hh24:mi:ss.ff3')";

			else if (parameter instanceof Date)
				return "to_date('" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(parameter)
						+ "', 'mm/dd/yyyy hh24:mi:ss')";

			else if (parameter instanceof Boolean)
				return ((Boolean) parameter).booleanValue() ? "1" : "0";

			else
				return parameter.toString();

		}
	}

}
