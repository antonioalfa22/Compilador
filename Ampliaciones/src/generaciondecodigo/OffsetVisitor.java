package generaciondecodigo;

import ast.definicion.Campo;
import ast.definicion.DefFuncion;
import ast.definicion.DefVariable;
import ast.tipo.TipoFuncion;
import ast.tipo.TipoStruct;
import visitor.AbstractVisitor;

public class OffsetVisitor extends AbstractVisitor {

	private int offsetGlobal = 0;
	private int offsetLocal = 0;

	@Override
	public Object visit(DefVariable e, Object param) {
		e.getTipo().accept(this, param);
		if (e.getAmbito() == 0) { // Variable global
			e.setOffset(offsetGlobal);
			offsetGlobal += e.getTipo().getNumeroBytes();
		} else if (e.getAmbito() > 0) { // Variable local
			offsetLocal -= e.getTipo().getNumeroBytes();
			e.setOffset(offsetLocal);
		}
		return null;
	}

	@Override
	public Object visit(TipoFuncion e, Object param) {
		int temp = 4;
		int size = 0;
		for (int i = (e.getParams().size() - 1); i >= 0; i--) {
			DefVariable parametro = e.getParams().get(i);
			parametro.setOffset(temp);
			temp += parametro.getTipo().getNumeroBytes();
			size += parametro.getTipo().getNumeroBytes();
		}
		return size;
	}

	@Override
	public Object visit(TipoStruct e, Object param) {
		e.getCampos().stream().forEach(x -> x.accept(this, param));
		int temp = 0;
		for(Campo c : e.getCampos()) {
			c.setOffset(temp);
			temp += c.getTipo().getNumeroBytes();
		}
		return null;
	}

	@Override
	public Object visit(DefFuncion e, Object param) {
		e.setParametersSize((int)e.getTipo().accept(this, param));
		int enter = 0;
		for(DefVariable df : e.getDefiniciones()) {
			enter += df.getTipo().getNumeroBytes();
		}
		e.setTotalLocalVariableSize(enter);
		offsetLocal=0; 
		e.getDefiniciones().stream().forEach(x -> x.accept(this, param));
		offsetLocal=0;
		return null;
	}
}
