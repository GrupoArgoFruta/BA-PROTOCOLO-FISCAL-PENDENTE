package br.com.argo.protocolo.fisc.pendente.controller;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;


public class DoActionFsicalPendenteController implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        Registro[] linhas = ctx.getLinhas();
        JapeSession.SessionHandle hnd = JapeSession.open();

        try {
            for (Registro registro : linhas) {
                BigDecimal nUnico = (BigDecimal) registro.getCampo("NUNOTA");

                if (ctx.confirmarSimNao("Protocolo Fiscal",
                        "Deseja marcar como pendente?", 0)) {
                    // SIM → atualiza flag pra "S"
                    ProtocoloFsicalPendente(nUnico, "S");
                    ctx.setMensagemRetorno("Nota " + nUnico + " marcada como PENDENTE.");
                } else {
                    // NÃO → atualiza flag pra "N"
                    ProtocoloFsicalPendente(nUnico, "N");
                    ctx.setMensagemRetorno("Nota " + nUnico + " marcada como NÃO pendente.");
                }
            }

        } catch (IllegalStateException ise) {
            throw ise; // deixa o Sankhya exibir o popup
        } catch (Exception e) {
            e.printStackTrace();
            ctx.setMensagemRetorno("Erro: " + e.getMessage());
        } finally {
            JapeSession.close(hnd);
        }
    }
    public void ProtocoloFsicalPendente(BigDecimal nUnico, String pendente )throws Exception {
        JapeSession.SessionHandle hnd = null;
        JdbcWrapper jdbc = null;
        NativeSql query = null;
        try {
            // Montando a query de atualização
            String update = "UPDATE TGFCAB SET AD_PROFISC_PENDENTE = :AD_PROFISC_PENDENTE WHERE NUNOTA = :NUNOTA";
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);
            hnd.setFindersMaxRows(-1);
            EntityFacade entity = EntityFacadeFactory.getDWFFacade();
            jdbc = entity.getJdbcWrapper();
            jdbc.openSession();

            query = new NativeSql(jdbc);
            query.appendSql(update);

            // Definindo os parâmetros nomeados
            query.setNamedParameter("AD_PROFISC_PENDENTE", pendente);
            query.setNamedParameter("NUNOTA", nUnico);

            query.executeUpdate(); // Executando o update

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new Exception("Erro ao executar a atualização Protocolo fiscal pendente : " + e.getMessage());
        } finally {
            JapeSession.close(hnd);
            JdbcWrapper.closeSession(jdbc);
            NativeSql.releaseResources(query);
        }


    }
}