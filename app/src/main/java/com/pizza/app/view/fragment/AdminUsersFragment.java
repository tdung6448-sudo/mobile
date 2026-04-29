package com.pizza.app.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pizza.app.databinding.FragmentAdminUsersBinding;
import com.pizza.app.view.adapter.AdminUserAdapter;
import com.pizza.app.viewmodel.AdminViewModel;

public class AdminUsersFragment extends Fragment {

    private FragmentAdminUsersBinding binding;
    private AdminViewModel            viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding   = FragmentAdminUsersBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdminUserAdapter adapter = new AdminUserAdapter(user -> {
            boolean willBlock = !user.isBlocked();
            String msg = willBlock
                    ? "Khoá tài khoản " + user.getName() + "?"
                    : "Mở khoá tài khoản " + user.getName() + "?";

            new AlertDialog.Builder(requireContext())
                    .setTitle(willBlock ? "Khoá tài khoản" : "Mở khoá")
                    .setMessage(msg)
                    .setPositiveButton("Xác nhận", (d, w) ->
                            viewModel.setUserBlocked(user.getUid(), willBlock)
                                    .observe(getViewLifecycleOwner(), r -> {}))
                    .setNegativeButton("Huỷ", null)
                    .show();
        });

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUsers.setAdapter(adapter);

        viewModel.getAllUsers().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
